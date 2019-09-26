/*
 *
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of FreeHealthConnector.
 *
 * FreeHealthConnector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation.
 *
 * FreeHealthConnector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with FreeHealthConnector.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.taktik.freehealth.middleware.service.impl

import be.cin.encrypted.BusinessContent
import be.cin.encrypted.EncryptedKnownContent
import be.fgov.ehealth.etee.crypto.utils.KeyManager
import be.fgov.ehealth.mycarenet.commons.core.v3.CareProviderType
import be.fgov.ehealth.mycarenet.commons.core.v3.CommonInputType
import be.fgov.ehealth.mycarenet.commons.core.v3.IdType
import be.fgov.ehealth.mycarenet.commons.core.v3.LicenseType
import be.fgov.ehealth.mycarenet.commons.core.v3.NihiiType
import be.fgov.ehealth.mycarenet.commons.core.v3.OriginType
import be.fgov.ehealth.mycarenet.commons.core.v3.PackageType
import be.fgov.ehealth.mycarenet.commons.core.v3.RequestType
import be.fgov.ehealth.mycarenet.commons.core.v3.ValueRefString
import be.fgov.ehealth.mycarenet.memberdata.protocol.v1.MemberDataConsultationRequest
import com.google.gson.Gson
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl
import com.sun.xml.messaging.saaj.soap.impl.ElementImpl
import com.sun.xml.messaging.saaj.soap.ver1_1.DetailEntry1_1Impl
import ma.glasnost.orika.MapperFacade
import org.taktik.icure.cin.saml.oasis.names.tc.saml._2_0.assertion.NameIDType
import org.taktik.icure.cin.saml.oasis.names.tc.saml._2_0.assertion.Subject
import org.taktik.icure.cin.saml.oasis.names.tc.saml._2_0.assertion.SubjectConfirmation
import org.taktik.icure.cin.saml.oasis.names.tc.saml._2_0.assertion.SubjectConfirmationDataType
import org.taktik.icure.cin.saml.oasis.names.tc.saml._2_0.protocol.AttributeQuery
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.taktik.connector.business.memberdata.builders.impl.ResponseObjectBuilderImpl
import org.taktik.connector.business.memberdata.domain.MemberDataBuilderResponse
import org.taktik.connector.business.memberdata.validators.impl.MemberDataXmlValidatorImpl
import org.taktik.connector.business.mycarenetcommons.mapper.v3.BlobMapper
import org.taktik.connector.business.mycarenetdomaincommons.builders.BlobBuilderFactory
import org.taktik.connector.business.mycarenetdomaincommons.util.McnConfigUtil
import org.taktik.connector.business.mycarenetdomaincommons.util.PropertyUtil
import org.taktik.connector.technical.config.ConfigFactory
import org.taktik.connector.technical.idgenerator.IdGeneratorFactory
import org.taktik.connector.technical.service.etee.Crypto
import org.taktik.connector.technical.service.etee.CryptoFactory
import org.taktik.connector.technical.service.keydepot.KeyDepotService
import org.taktik.connector.technical.service.keydepot.impl.KeyDepotManagerImpl
import org.taktik.connector.technical.service.sts.security.impl.KeyStoreCredential
import org.taktik.connector.technical.utils.ConnectorXmlUtils
import org.taktik.connector.technical.utils.IdentifierType
import org.taktik.freehealth.middleware.dao.User
import org.taktik.freehealth.middleware.dto.mycarenet.MycarenetError
import org.taktik.freehealth.middleware.exception.MissingTokenException
import org.taktik.freehealth.middleware.service.MemberDataService
import org.taktik.freehealth.middleware.service.STSService
import org.taktik.icure.cin.saml.extensions.Facet
import org.taktik.icure.cin.saml.oasis.names.tc.saml._2_0.assertion.Assertion
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.util.Date
import java.util.UUID
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

@Service
class MemberDataServiceImpl(val stsService: STSService, keyDepotService: KeyDepotService, val mapper: MapperFacade) : MemberDataService {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val GenInsErrors =
        Gson().fromJson(
            this.javaClass.getResourceAsStream("/be/errors/GenInsErrors.json").reader(Charsets.UTF_8),
            arrayOf<MycarenetError>().javaClass
        ).associateBy({ it.uid }, { it })
    private val xPathfactory = XPathFactory.newInstance()
    private val config = ConfigFactory.getConfigValidator(listOf())
    private val keyDepotManager = KeyDepotManagerImpl.getInstance(keyDepotService)
    private val memberDataService = org.taktik.connector.business.memberdata.service.impl.MemberDataServiceImpl()

    override fun getMemberData(
        keystoreId: UUID,
        tokenId: UUID,
        hcpQuality: String,
        hcpNihii: String,
        hcpSsin: String,
        hcpName: String,
        passPhrase: String,
        patientSsin: String?,
        io: String?,
        ioMembership: String?,
        startDate: Date?,
        endDate: Date?,
        hospitalized: Boolean
    ): List<Assertion> {
        val encryptRequest = false
        require(
            hcpQuality == "doctor" ||
                hcpQuality == "nurse" ||
                hcpQuality == "physiotherapist" ||
                hcpQuality == "dentist" ||
                hcpQuality == "logopedist" ||
                hcpQuality == "trussmaker" ||
                hcpQuality == "orthopedist" ||
                hcpQuality == "midwife" ||
                hcpQuality == "optician" ||
                hcpQuality == "podologist" ||
                hcpQuality == "dietician" ||
                hcpQuality == "hospital" ||
                hcpQuality == "groupofnurses" ||
                hcpQuality == "labo" ||
                hcpQuality == "retirement" ||
                hcpQuality == "otdpharmacy" ||
                hcpQuality == "medicalhouse" ||
                hcpQuality == "groupofdoctors" ||
                hcpQuality == "psychiatrichouse" ||
                hcpQuality == "guardpost" ||
                hcpQuality == "ambulanceservice"
        ) { "hcpQuality is invalid" }

        val samlToken =
            stsService.getSAMLToken(tokenId, keystoreId, passPhrase)
                ?: throw MissingTokenException("Cannot obtain token for Genins operations")
        val keystore = stsService.getKeyStore(keystoreId, passPhrase)!!

        val credential = KeyStoreCredential(keystoreId, keystore, "authentication", passPhrase)
        val hokPrivateKeys = KeyManager.getDecryptionKeys(keystore, passPhrase.toCharArray())
        val crypto = CryptoFactory.getCrypto(credential, hokPrivateKeys)

        assert(patientSsin != null || io != null && ioMembership != null)

        val principal = SecurityContextHolder.getContext().authentication?.principal as? User
        val packageInfo = McnConfigUtil.retrievePackageInfo("genins", principal?.mcnLicense, principal?.mcnPassword)

        log.info("getMemberData called with principal "+(principal?:"<ANONYMOUS>")+" and license " + (principal?.mcnLicense ?: "<DEFAULT>"))

        val inputRef = "" + IdGeneratorFactory.getIdGenerator().generateId()
        val requestId = IdGeneratorFactory.getIdGenerator("xsid").generateId()

        val request = MemberDataConsultationRequest().apply {
            val issueInstantDateTime = DateTime()
            val issueInstant = XMLGregorianCalendarImpl(issueInstantDateTime.toGregorianCalendar())

            commonInput = CommonInputType().apply {
                request =
                    RequestType().apply { isIsTest = false /*config.getProperty("endpoint.genins")?.contains("-acpt") ?: false*/ }
                inputReference = inputRef
                origin = OriginType().apply {
                    `package` = PackageType().apply {
                        license = LicenseType().apply {
                            username = packageInfo.userName
                            password = packageInfo.password
                        }
                        name = ValueRefString().apply { value = packageInfo.packageName }
                    }
                    config.getProperty("mycarenet.${PropertyUtil.retrieveProjectNameToUse("genins","mycarenet.")}.site.id")?.let{
                        if (it.isNotBlank()) {
                            siteID = ValueRefString().apply { value = it }
                        }
                    }
                    careProvider = CareProviderType().apply {
                        if(hcpQuality == "guardpost") {
                            // nihii11 is required with guardpost
                            nihii =
                                NihiiType().apply {
                                    quality = hcpQuality; value =
                                    ValueRefString().apply { value = hcpNihii.padEnd(11, '0') }
                                }
                            organization = IdType().apply {
                                nihii =
                                    NihiiType().apply {
                                        quality = hcpQuality; value =
                                        ValueRefString().apply { value = hcpNihii.padEnd(11, '0') }
                                    }
                            }
                        }else{
                            nihii =
                                NihiiType().apply {
                                    quality = hcpQuality; value =
                                    ValueRefString().apply { value = hcpNihii }
                                }
                            physicalPerson = IdType().apply {
                                name = ValueRefString().apply { value = hcpName }
                                ssin = ValueRefString().apply { value = hcpSsin }
                                nihii =
                                    NihiiType().apply {
                                        quality = hcpQuality; value =
                                        ValueRefString().apply { value = hcpNihii }
                                    }
                            }
                        }
                    }
                }
            }
            this.id = requestId
            this.issueInstant = issueInstantDateTime

            val attrQuery = AttributeQuery().apply {
                id = "_$inputRef"
                this.issueInstant = issueInstant
                this.version = "2.0"
                extensions = org.taktik.icure.cin.saml.extensions.ExtensionsType().apply {
                    this.facets.add(Facet().apply {
                        id = "urn:be:cin:nippin:insurability"
                        dimensions.add(Facet.Dimension().apply { id = "requestType"; value = "information" })
                        dimensions.add(Facet.Dimension().apply { id = "contactType"; value = "other" })
                    })
                }
                issuer = NameIDType().apply {
                    this.format = "urn:be:cin:nippin:nihii11"
                    this.value = hcpNihii.padEnd(11, '0')
                }
                subject = Subject().apply {
                    this.nameID = NameIDType().apply {
                        when {
                            patientSsin != null && io == null -> {
                                format = "urn:be:fgov:person:ssin"
                                value = patientSsin
                            }
                            patientSsin != null && io != null -> {
                                format = "urn:be:cin:nippin:member:ssin@mut"
                                value = "$patientSsin@$io"
                            }
                            ioMembership != null && io != null -> {
                                format = "urn:be:cin:nippin:careReceiver:registrationNumber@mut"
                                value = "$ioMembership@$io"
                            }
                        }
                    }
                    subjectConfirmations.add(SubjectConfirmation().apply {
                        method = "urn:be:cin:nippin:memberIdentification"
                        subjectConfirmationData = SubjectConfirmationDataType().apply {
                            (startDate ?: Date()).let { notBefore = XMLGregorianCalendarImpl(DateTime(it.time, DateTimeZone.forID("Europe/Brussels")).toGregorianCalendar()) }
                            endDate?.let { notOnOrAfter = XMLGregorianCalendarImpl(DateTime(it.time, DateTimeZone.forID("Europe/Brussels")).toGregorianCalendar()) }
                        }
                    })
                }
            }

            val detailId = "_" + IdGeneratorFactory.getIdGenerator("uuid").generateId();
            val blobBuilder = BlobBuilderFactory.getBlobBuilder("memberdata")

            this.detail = ConnectorXmlUtils.toByteArray(attrQuery as Any).let {aqb ->
                if (encryptRequest) {
                    val identifierTypeString = config.getProperty("memberdata.keydepot.identifiertype", "CBE")
                    val identifierValue = config.getLongProperty("memberdata.keydepot.identifiervalue", 820563481L)
                    val applicationId = config.getProperty("memberdata.keydepot.application", "MYCARENET")
                    val identifierSource = 48
                    val identifier = IdentifierType.lookup(identifierTypeString, null as String?, identifierSource)

                    val mbEtk = if (identifier == null) {
                        throw IllegalStateException("invalid configuration : identifier with type ]$identifierTypeString[ for source ETKDEPOT not found")
                    } else {
                        keyDepotManager.getEtkSet(IdentifierType.CBE, identifierValue, applicationId, keystoreId, false)
                    }

                    crypto.seal(Crypto.SigningPolicySelector.WITH_NON_REPUDIATION, mbEtk, ConnectorXmlUtils.toByteArray(
                        EncryptedKnownContent().apply {
                        replyToEtk = keyDepotManager.getETK(credential, keystoreId).encoded
                        businessContent = BusinessContent().apply {
                            id = detailId
                            value = aqb
                        }
                    })).let {
                        BlobMapper.mapBlobTypefromBlob(blobBuilder.build(it, "none", detailId, "text/xml", "MDA", "encryptedForKnownBED"))
                    }
                } else BlobMapper.mapBlobTypefromBlob(blobBuilder.build(aqb, "none", detailId, "text/xml", "MDA"))
            }
        }
        MemberDataXmlValidatorImpl().validate(request)

        val consultMemberData = memberDataService.consultMemberData(samlToken, request)

        return ResponseObjectBuilderImpl().handleConsultationResponse(consultMemberData, crypto).let {
            it.assertions
        }
    }

    private fun extractError(sendTransactionRequest: ByteArray, ec: String, errorUrl: String?): Set<MycarenetError> {
        //For some reason... The path starts with ../../../../ which corresponds to the request
        return errorUrl?.let { url ->
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            val builder = factory.newDocumentBuilder()

            val xpath = xPathFactory()
            val expr = xpath.compile(url.replace(Regex("^\\.\\./\\.\\./\\.\\./\\.\\./"),"/gip:GetInsurabilityAsXmlOrFlatRequestType/gip:Request/")
                                         .replace(Regex("/(CareReceiverId|Inss|RegNrWithMut|Mutuality|InsurabilityRequestDetail|InsurabilityRequestType|Period|PeriodStart|PeriodEnd|InsurabilityContactType|InsurabilityReference)"),"/gic:$1"))
            val result = mutableSetOf<MycarenetError>()

            (expr.evaluate(
                builder.parse(ByteArrayInputStream(sendTransactionRequest)),
                XPathConstants.NODESET
                          ) as NodeList).let { it ->
                if (it.length > 0) {
                    var node = it.item(0)
                    val textContent = node.textContent
                    var base = "/" + nodeDescr(node)
                    while (node.parentNode != null && node.parentNode is Element) {
                        base = "/${nodeDescr(node.parentNode)}$base"
                        node = node.parentNode
                    }
                    val elements =
                        GenInsErrors.values.filter {(it.path == null || it.path == base) && it.code == ec }
                    elements.forEach { it.value = textContent }
                    result.addAll(elements)
                } else {
                    result.add(
                        MycarenetError(
                            code = ec,
                            path = url,
                            msgFr = "Erreur générique, xpath invalide",
                            msgNl = "Onbekend foutmelding, xpath ongeldig"
                                                                                     )
                              )
                }
            }
            result
        } ?: setOf()
    }

    private fun extractError(e: javax.xml.ws.soap.SOAPFaultException): Set<MycarenetError> {
        val result = mutableSetOf<MycarenetError>()

        e.fault.detail.detailEntries.forEach { it ->
            if(it != null) {
                val detailEntry = it as DetailEntry1_1Impl
                val codeElements = detailEntry.getElementsByTagName("Code")
                for (i in 0..(codeElements.length - 1)){
                    val codeElement = codeElements?.item(i) as ElementImpl
                    result.addAll(GenInsErrors.values.filter { it.code == codeElement.value })
                }
            }
        }
        return result
    }

    private fun nodeDescr(node: Node): String {
        val localName = node.localName ?: node.nodeName?.replace(Regex(".+?:(.+)"), "$1") ?: "unknown"

        return localName
    }

    private fun xPathFactory(): XPath {
        val xpath = xPathfactory.newXPath()
        xpath.namespaceContext = object : NamespaceContext {
            override fun getNamespaceURI(prefix: String?) = when (prefix) {
                "gic" -> "urn:be:fgov:ehealth:genericinsurability:core:v1"
                "gip" -> "urn:be:fgov:ehealth:genericinsurability:protocol:v1"
                else -> null
            }

            override fun getPrefix(namespaceURI: String?) = when (namespaceURI) {
                "urn:be:fgov:ehealth:genericinsurability:core:v1" -> "gic"
                "urn:be:fgov:ehealth:genericinsurability:protocol:v1" -> "gip"
                else -> null
            }

            override fun getPrefixes(namespaceURI: String?): Iterator<Any?> =
                when (namespaceURI) {
                    "urn:be:fgov:ehealth:genericinsurability:core:v1" -> listOf("gic").iterator()
                    "urn:be:fgov:ehealth:genericinsurability:protocol:v1" -> listOf("gip").iterator()
                    else -> listOf<String>().iterator()
                }
        }
        return xpath
    }
}