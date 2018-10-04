/*
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of iCureBackend.
 *
 * iCureBackend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * iCureBackend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCureBackend.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.taktik.freehealth.middleware.dto.efact

import org.taktik.freehealth.middleware.dto.efact.Acknowledgment
import org.taktik.freehealth.middleware.dto.efact.ErrorDetail
import org.taktik.freehealth.middleware.dto.efact.Receipt95
import org.taktik.freehealth.middleware.dto.efact.segments.Segment200Description
import org.taktik.freehealth.middleware.dto.efact.segments.Segment300Description
import org.taktik.freehealth.middleware.dto.efact.segments.Segment300ErrorDescription
import java.io.Serializable
import java.util.ArrayList

class ErrorMessage : Serializable {
    var acknowledgment: Acknowledgment? = null
    var receipts95: MutableList<Receipt95> = ArrayList()
    var errorDetails: MutableList<ErrorDetail> = ArrayList()
}
