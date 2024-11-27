/*
 * Copyright (C) 2007 Martin Willi
 * Hochschule fuer Technik Rapperswil
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.  See <http://www.fsf.org/copyleft/gpl.txt>.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

#include "ike_dpd.h"

#include <daemon.h>


typedef struct private_ike_dpd_t private_ike_dpd_t;

/**
 * Private members of a ike_dpd_t task.
 */
struct private_ike_dpd_t {

	/**
	 * Public methods and task_t interface.
	 */
	ike_dpd_t public;
};

METHOD(task_t, return_need_more, status_t,
	private_ike_dpd_t *this, message_t *message)
{
	return NEED_MORE;
}

static void get_traffic_values(chunk_t data, uint64_t *usage, uint64_t *limit)
{
	DBG2(DBG_DMN, "updated traffic usage: %B", &data);
	*usage = -1;
	*limit = -1;
	if (data.len == 9)
	{
		*usage = be64toh(*(uint64_t*)(data.ptr + 1));
		*limit = -1;
	}
	if (data.len == 18)
	{
		*usage = be64toh(*(uint64_t*)(data.ptr + 1));
		*limit = be64toh(*(uint64_t*)(data.ptr + 10));
	}
	DBG1(DBG_DMN, "updated traffic: usage = %d (bytes); limit = %d (bytes)", *usage, *limit);
}

METHOD(task_t, process_r, status_t,
	private_ike_dpd_t *this, message_t *message)
{
	enumerator_t *enumerator;
	payload_t *payload;

	enumerator = message->create_payload_enumerator(message);
	while (enumerator->enumerate(enumerator, &payload))
	{
		if (payload->get_type(payload) == PLV2_NOTIFY)
		{
			notify_payload_t *notify = (notify_payload_t*)payload;
			notify_type_t type = notify->get_notify_type(notify);

			switch (type)
			{
				case TRAFFIC_LIMIT_EXCEED:
				{
					DBG1(DBG_DMN, "traffic limit exceed: session will be killed");
					//notify to Java domain (UI)
					charon->bus->alert(charon->bus, ALERT_UPDATE_TRAFFIC, type, 0, 0);
				}
				case UPDATE_TRAFFIC_QUOTA:
				{
					chunk_t data = notify->get_notification_data(notify);
                    uint64_t usage, limit;
                    get_traffic_values(data, &usage, &limit);
					//notify to Java domain (UI)
					charon->bus->alert(charon->bus, ALERT_UPDATE_TRAFFIC, type, usage, limit);
				}
				default:
					break;
			}
		}
	}
	enumerator->destroy(enumerator);

	return NEED_MORE;
}

METHOD(task_t, get_type, task_type_t,
	private_ike_dpd_t *this)
{
	return TASK_IKE_DPD;
}


METHOD(task_t, migrate, void,
	private_ike_dpd_t *this, ike_sa_t *ike_sa)
{

}

METHOD(task_t, destroy, void,
	private_ike_dpd_t *this)
{
	free(this);
}

/*
 * Described in header.
 */
ike_dpd_t *ike_dpd_create(bool initiator)
{
	private_ike_dpd_t *this;

	INIT(this,
		.public = {
			.task = {
				.get_type = _get_type,
				.migrate = _migrate,
				.destroy = _destroy,
			},
		},
	);

	if (initiator)
	{
		this->public.task.build = _return_need_more;
		this->public.task.process = (void*)return_success;
	}
	else
	{
		this->public.task.build = (void*)return_success;
		this->public.task.process = _process_r;
	}

	return &this->public;
}
