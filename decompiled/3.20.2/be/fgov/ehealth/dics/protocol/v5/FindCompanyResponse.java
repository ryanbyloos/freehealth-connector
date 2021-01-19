package be.fgov.ehealth.dics.protocol.v5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "FindCompanyResponseType",
   propOrder = {"companies"}
)
@XmlRootElement(
   name = "FindCompanyResponse"
)
public class FindCompanyResponse extends DicsResponseType implements Serializable {
   private static final long serialVersionUID = 1L;
   @XmlElement(
      name = "Company"
   )
   protected List<ConsultCompanyType> companies;

   public List<ConsultCompanyType> getCompanies() {
      if (this.companies == null) {
         this.companies = new ArrayList();
      }

      return this.companies;
   }
}
