package be.business.connector.recipe.executor;

import be.business.connector.common.ApplicationConfig;
import be.business.connector.common.StandaloneRequestorProvider;
import be.business.connector.core.domain.IdentifierTypes;
import be.business.connector.core.domain.KgssIdentifierType;
import be.business.connector.core.exceptions.IntegrationModuleException;
import be.business.connector.core.handlers.InsurabilityHandler;
import be.business.connector.core.utils.Exceptionutils;
import be.business.connector.core.utils.I18nHelper;
import be.business.connector.core.utils.MarshallerHelper;
import be.business.connector.recipe.executor.domain.GetPrescriptionForExecutorResult;
import be.business.connector.recipe.executor.services.RecipeExecutorServiceV3Impl;
import be.business.connector.recipe.utils.RidValidator;
import be.ehealth.technicalconnector.service.kgss.domain.KeyResult;
import be.fgov.ehealth.etee.crypto.encrypt.EncryptionToken;
import be.fgov.ehealth.recipe.core.v3.ExecutorServiceAdministrativeInformationType;
import be.fgov.ehealth.recipe.core.v3.SecuredContentType;
import be.fgov.ehealth.recipe.protocol.v3.CreateFeedbackRequest;
import be.fgov.ehealth.recipe.protocol.v3.GetPrescriptionForExecutorRequest;
import be.fgov.ehealth.recipe.protocol.v3.GetPrescriptionForExecutorResponse;
import be.fgov.ehealth.recipe.protocol.v3.ListNotificationsRequest;
import be.fgov.ehealth.recipe.protocol.v3.ListNotificationsResponse;
import be.fgov.ehealth.recipe.protocol.v3.MarkAsArchivedRequest;
import be.fgov.ehealth.recipe.protocol.v3.MarkAsDeliveredRequest;
import be.fgov.ehealth.recipe.protocol.v3.MarkAsUnDeliveredRequest;
import be.fgov.ehealth.recipe.protocol.v3.RevokePrescriptionForExecutorRequest;
import be.recipe.services.executor.GetPrescriptionForExecutorResultSealed;
import be.recipe.services.executor.ListNotificationsItem;
import com.sun.xml.ws.client.ClientTransportException;
import java.lang.annotation.Annotation;
import java.util.List;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.runtime.internal.Conversions;
import org.aspectj.runtime.reflect.Factory;
import org.perf4j.aop.Profiled;
import org.perf4j.log4j.aop.TimingAspect;

public class ExecutorIntegrationModuleV3Impl extends AbstractExecutorIntegrationModule implements ExecutorIntegrationModule {
   private static final Logger LOG;
   // $FF: synthetic field
   private static final StaticPart ajc$tjp_0;
   // $FF: synthetic field
   private static Annotation ajc$anno$0;
   // $FF: synthetic field
   private static final StaticPart ajc$tjp_1;
   // $FF: synthetic field
   private static Annotation ajc$anno$1;
   // $FF: synthetic field
   private static final StaticPart ajc$tjp_2;
   // $FF: synthetic field
   private static Annotation ajc$anno$2;
   // $FF: synthetic field
   private static final StaticPart ajc$tjp_3;
   // $FF: synthetic field
   private static Annotation ajc$anno$3;
   // $FF: synthetic field
   private static final StaticPart ajc$tjp_4;
   // $FF: synthetic field
   private static Annotation ajc$anno$4;
   // $FF: synthetic field
   private static final StaticPart ajc$tjp_5;
   // $FF: synthetic field
   private static Annotation ajc$anno$5;
   // $FF: synthetic field
   private static final StaticPart ajc$tjp_6;
   // $FF: synthetic field
   private static Annotation ajc$anno$6;

   static {
      ajc$preClinit();
      LOG = Logger.getLogger(ExecutorIntegrationModuleV3Impl.class);
   }

   public ExecutorIntegrationModuleV3Impl() throws IntegrationModuleException {
      LOG.info("*************** Executor V3 System module init correctly *******************");
   }

   @Profiled(
      logFailuresSeparately = true,
      tag = "ExecutorIntegrationModule#getPrescription"
   )
   public GetPrescriptionForExecutorResult getPrescription(String rid) throws IntegrationModuleException {
      JoinPoint var8 = Factory.makeJP(ajc$tjp_0, this, this, rid);
      TimingAspect var10000 = TimingAspect.aspectOf();
      Object[] var9 = new Object[]{this, rid, var8};
      ProceedingJoinPoint var10001 = (new ExecutorIntegrationModuleV3Impl$AjcClosure1(var9)).linkClosureAndJoinPoint(69648);
      Annotation var10002 = ajc$anno$0;
      if (var10002 == null) {
         var10002 = ajc$anno$0 = ExecutorIntegrationModuleV3Impl.class.getDeclaredMethod("getPrescription", String.class).getAnnotation(Profiled.class);
      }

      return (GetPrescriptionForExecutorResult)var10000.doPerfLogging(var10001, (Profiled)var10002);
   }

   protected GetPrescriptionForExecutorResult createGetPrescriptionForExecutorResult(byte[] securedContent) throws IntegrationModuleException {
      MarshallerHelper<GetPrescriptionForExecutorResultSealed, Object> marshaller = new MarshallerHelper(GetPrescriptionForExecutorResultSealed.class, Object.class);
      String requestorIdInformation = StandaloneRequestorProvider.getRequestorIdInformation();
      String requestorTypeInformation = StandaloneRequestorProvider.getRequestorTypeInformation();
      GetPrescriptionForExecutorResultSealed sealedResult = (GetPrescriptionForExecutorResultSealed)marshaller.unsealWithSymmKey(securedContent, this.getSymmKey());
      KeyResult key = this.getKeyFromKgss(sealedResult.getEncryptionKeyId(), ((EncryptionToken)this.getEtkHelper().getEtks(KgssIdentifierType.NIHII_PHARMACY, requestorIdInformation).get(0)).getEncoded());
      byte[] unsealedPrescription = this.unsealWithSymKey(sealedResult, key, requestorIdInformation, requestorTypeInformation);
      GetPrescriptionForExecutorResult finalResult = new GetPrescriptionForExecutorResult(sealedResult);
      finalResult.setSealedContent(sealedResult.getPrescription());
      finalResult.setPrescription(unsealedPrescription);
      finalResult.setEncryptionKey(key.getSecretKey().getEncoded());
      finalResult.setInsurabilityResponse(InsurabilityHandler.getInsurability());
      finalResult.setMessageId(InsurabilityHandler.getMessageId());
      return finalResult;
   }

   @Profiled(
      logFailuresSeparately = true,
      tag = "0.ExecutorIntegrationModule#markAsArchived"
   )
   public void markAsArchived(String rid) throws IntegrationModuleException {
      JoinPoint var6 = Factory.makeJP(ajc$tjp_1, this, this, rid);
      TimingAspect var10000 = TimingAspect.aspectOf();
      Object[] var7 = new Object[]{this, rid, var6};
      ProceedingJoinPoint var10001 = (new ExecutorIntegrationModuleV3Impl$AjcClosure3(var7)).linkClosureAndJoinPoint(69648);
      Annotation var10002 = ajc$anno$1;
      if (var10002 == null) {
         var10002 = ajc$anno$1 = ExecutorIntegrationModuleV3Impl.class.getDeclaredMethod("markAsArchived", String.class).getAnnotation(Profiled.class);
      }

      var10000.doPerfLogging(var10001, (Profiled)var10002);
   }

   @Profiled(
      logFailuresSeparately = true,
      tag = "0.ExecutorIntegrationModule#markAsDelivered"
   )
   public void markAsDelivered(String rid) throws IntegrationModuleException {
      JoinPoint var6 = Factory.makeJP(ajc$tjp_2, this, this, rid);
      TimingAspect var10000 = TimingAspect.aspectOf();
      Object[] var7 = new Object[]{this, rid, var6};
      ProceedingJoinPoint var10001 = (new ExecutorIntegrationModuleV3Impl$AjcClosure5(var7)).linkClosureAndJoinPoint(69648);
      Annotation var10002 = ajc$anno$2;
      if (var10002 == null) {
         var10002 = ajc$anno$2 = ExecutorIntegrationModuleV3Impl.class.getDeclaredMethod("markAsDelivered", String.class).getAnnotation(Profiled.class);
      }

      var10000.doPerfLogging(var10001, (Profiled)var10002);
   }

   @Profiled(
      logFailuresSeparately = true,
      tag = "0.ExecutorIntegrationModule#markAsUndelivered"
   )
   public void markAsUndelivered(String rid) throws IntegrationModuleException {
      JoinPoint var6 = Factory.makeJP(ajc$tjp_3, this, this, rid);
      TimingAspect var10000 = TimingAspect.aspectOf();
      Object[] var7 = new Object[]{this, rid, var6};
      ProceedingJoinPoint var10001 = (new ExecutorIntegrationModuleV3Impl$AjcClosure7(var7)).linkClosureAndJoinPoint(69648);
      Annotation var10002 = ajc$anno$3;
      if (var10002 == null) {
         var10002 = ajc$anno$3 = ExecutorIntegrationModuleV3Impl.class.getDeclaredMethod("markAsUndelivered", String.class).getAnnotation(Profiled.class);
      }

      var10000.doPerfLogging(var10001, (Profiled)var10002);
   }

   @Profiled(
      logFailuresSeparately = true,
      tag = "0.ExecutorIntegrationModule#revokePrescription"
   )
   public void revokePrescription(String rid, String reason) throws IntegrationModuleException {
      JoinPoint var8 = Factory.makeJP(ajc$tjp_4, this, this, rid, reason);
      TimingAspect var10000 = TimingAspect.aspectOf();
      Object[] var9 = new Object[]{this, rid, reason, var8};
      ProceedingJoinPoint var10001 = (new ExecutorIntegrationModuleV3Impl$AjcClosure9(var9)).linkClosureAndJoinPoint(69648);
      Annotation var10002 = ajc$anno$4;
      if (var10002 == null) {
         var10002 = ajc$anno$4 = ExecutorIntegrationModuleV3Impl.class.getDeclaredMethod("revokePrescription", String.class, String.class).getAnnotation(Profiled.class);
      }

      var10000.doPerfLogging(var10001, (Profiled)var10002);
   }

   @Profiled(
      logFailuresSeparately = true,
      tag = "0.ExecutorIntegrationModule#listNotifications"
   )
   public List<ListNotificationsItem> listNotifications(boolean readFlag) throws IntegrationModuleException {
      JoinPoint var7 = Factory.makeJP(ajc$tjp_5, this, this, Conversions.booleanObject(readFlag));
      TimingAspect var10000 = TimingAspect.aspectOf();
      Object[] var8 = new Object[]{this, Conversions.booleanObject(readFlag), var7};
      ProceedingJoinPoint var10001 = (new ExecutorIntegrationModuleV3Impl$AjcClosure11(var8)).linkClosureAndJoinPoint(69648);
      Annotation var10002 = ajc$anno$5;
      if (var10002 == null) {
         var10002 = ajc$anno$5 = ExecutorIntegrationModuleV3Impl.class.getDeclaredMethod("listNotifications", Boolean.TYPE).getAnnotation(Profiled.class);
      }

      return (List)var10000.doPerfLogging(var10001, (Profiled)var10002);
   }

   @Profiled(
      logFailuresSeparately = true,
      tag = "0.ExecutorIntegrationModule#createFeedback"
   )
   public void createFeedback(String prescriberId, String rid, byte[] feedbackText) throws IntegrationModuleException {
      StaticPart var10000 = ajc$tjp_6;
      Object[] var15 = new Object[]{prescriberId, rid, feedbackText};
      JoinPoint var14 = Factory.makeJP(var10000, this, this, var15);
      TimingAspect var17 = TimingAspect.aspectOf();
      Object[] var16 = new Object[]{this, prescriberId, rid, feedbackText, var14};
      ProceedingJoinPoint var10001 = (new ExecutorIntegrationModuleV3Impl$AjcClosure13(var16)).linkClosureAndJoinPoint(69648);
      Annotation var10002 = ajc$anno$6;
      if (var10002 == null) {
         var10002 = ajc$anno$6 = ExecutorIntegrationModuleV3Impl.class.getDeclaredMethod("createFeedback", String.class, String.class, byte[].class).getAnnotation(Profiled.class);
      }

      var17.doPerfLogging(var10001, (Profiled)var10002);
   }

   private SecuredContentType createSecuredContentType(byte[] content) {
      SecuredContentType secured = new SecuredContentType();
      secured.setSecuredContent(content);
      return secured;
   }

   private ExecutorServiceAdministrativeInformationType getAdministrativeInfo(String rid) {
      GetPrescriptionForExecutorResult prescription = (GetPrescriptionForExecutorResult)getPrescriptionCache().get(rid);
      ExecutorServiceAdministrativeInformationType info = new ExecutorServiceAdministrativeInformationType();
      if (prescription != null) {
         info.setPatientIdentifier(this.createIdentifierType(prescription.getPatientId(), IdentifierTypes.SSIN.name()));
         info.setPrescriberIdentifier(this.createIdentifierType(prescription.getPrescriberId(), IdentifierTypes.NIHII11.name()));
      } else {
         info.setPatientIdentifier(this.createIdentifierType("72081061175", IdentifierTypes.SSIN.name()));
         info.setPrescriberIdentifier(this.createIdentifierType("10998018001", IdentifierTypes.NIHII11.name()));
      }

      return info;
   }

   // $FF: synthetic method
   static final GetPrescriptionForExecutorResult getPrescription_aroundBody0(ExecutorIntegrationModuleV3Impl ajc$this, String rid, JoinPoint var2) {
      RidValidator.validateRid(rid);
      ApplicationConfig.getInstance().assertValidSession();
      InsurabilityHandler.setInsurability((String)null);
      InsurabilityHandler.setMessageId((String)null);

      try {
         byte[] sealedGetPrescriptionForExecutorParam = ajc$this.getSealedGetPrescriptionForExecutorParam(rid);
         GetPrescriptionForExecutorRequest request = new GetPrescriptionForExecutorRequest();
         request.setDisablePatientInsurabilityCheckParam(Boolean.parseBoolean(ajc$this.getPropertyHandler().getProperty("patient.insurability.disable")));
         request.setSecuredGetPrescriptionForExecutorRequest(ajc$this.createSecuredContentType(sealedGetPrescriptionForExecutorParam));
         GetPrescriptionForExecutorResponse response = null;

         try {
            response = RecipeExecutorServiceV3Impl.getInstance().getPrescriptionForExecutor(request);
         } catch (ClientTransportException var12) {
            throw new IntegrationModuleException(I18nHelper.getLabel("error.connection.executor"), var12);
         }

         ajc$this.checkStatus(response);
         byte[] securedContent = response.getSecuredGetPrescriptionForExecutorResponse().getSecuredContent();
         GetPrescriptionForExecutorResult finalResult = ajc$this.createGetPrescriptionForExecutorResult(securedContent);
         getPrescriptionCache().put(rid, finalResult);
         return finalResult;
      } catch (Throwable var13) {
         Exceptionutils.errorHandler(var13);
         return null;
      }
   }

   // $FF: synthetic method
   static final void markAsArchived_aroundBody2(ExecutorIntegrationModuleV3Impl ajc$this, String rid, JoinPoint var2) {
      ApplicationConfig.getInstance().assertValidSession();

      try {
         byte[] sealedMarkAsArchivedParam = ajc$this.getSealedMarkAsArchivedParam(rid);
         MarkAsArchivedRequest request = new MarkAsArchivedRequest();
         request.setSecuredMarkAsArchivedRequest(ajc$this.createSecuredContentType(sealedMarkAsArchivedParam));
         request.setAdministrativeInformation(ajc$this.getAdministrativeInfo(rid));

         try {
            ajc$this.checkStatus(RecipeExecutorServiceV3Impl.getInstance().markAsArchived(request));
         } catch (ClientTransportException var8) {
            throw new IntegrationModuleException(I18nHelper.getLabel("error.connection.executor"), var8);
         }
      } catch (Throwable var9) {
         Exceptionutils.errorHandler(var9);
      }

   }

   // $FF: synthetic method
   static final void markAsDelivered_aroundBody4(ExecutorIntegrationModuleV3Impl ajc$this, String rid, JoinPoint var2) {
      ApplicationConfig.getInstance().assertValidSession();

      try {
         byte[] sealedMarkAsDeliveredParam = ajc$this.getSealedMarkAsDeliveredParam(rid);
         MarkAsDeliveredRequest request = new MarkAsDeliveredRequest();
         request.setSecuredMarkAsDeliveredRequest(ajc$this.createSecuredContentType(sealedMarkAsDeliveredParam));
         request.setAdministrativeInformation(ajc$this.getAdministrativeInfo(rid));

         try {
            ajc$this.checkStatus(RecipeExecutorServiceV3Impl.getInstance().markAsDelivered(request));
         } catch (ClientTransportException var8) {
            throw new IntegrationModuleException(I18nHelper.getLabel("error.connection.executor"), var8);
         }
      } catch (Throwable var9) {
         Exceptionutils.errorHandler(var9);
      }

   }

   // $FF: synthetic method
   static final void markAsUndelivered_aroundBody6(ExecutorIntegrationModuleV3Impl ajc$this, String rid, JoinPoint var2) {
      ApplicationConfig.getInstance().assertValidSession();

      try {
         byte[] sealedMarkAsUndeliveredParam = ajc$this.getSealedMarkAsUnDeliveredParam(rid);
         MarkAsUnDeliveredRequest request = new MarkAsUnDeliveredRequest();
         request.setSecuredMarkAsUnDeliveredRequest(ajc$this.createSecuredContentType(sealedMarkAsUndeliveredParam));
         request.setAdministrativeInformation(ajc$this.getAdministrativeInfo(rid));

         try {
            ajc$this.checkStatus(RecipeExecutorServiceV3Impl.getInstance().markAsUnDelivered(request));
         } catch (ClientTransportException var8) {
            throw new IntegrationModuleException(I18nHelper.getLabel("error.connection.executor"), var8);
         }
      } catch (Throwable var9) {
         Exceptionutils.errorHandler(var9);
      }

   }

   // $FF: synthetic method
   static final void revokePrescription_aroundBody8(ExecutorIntegrationModuleV3Impl ajc$this, String rid, String reason, JoinPoint var3) {
      ApplicationConfig.getInstance().assertValidSession();

      try {
         byte[] sealedRevokePrescriptionParam = ajc$this.getSealedRevokePrescriptionParam(rid, reason);
         RevokePrescriptionForExecutorRequest request = new RevokePrescriptionForExecutorRequest();
         request.setAdministrativeInformation(ajc$this.getAdministrativeInfo(rid));
         request.setSecuredRevokePrescriptionRequest(ajc$this.createSecuredContentType(sealedRevokePrescriptionParam));

         try {
            ajc$this.checkStatus(RecipeExecutorServiceV3Impl.getInstance().revokePrescriptionForExecutor(request));
         } catch (ClientTransportException var9) {
            throw new IntegrationModuleException(I18nHelper.getLabel("error.connection.executor"), var9);
         }
      } catch (Throwable var10) {
         Exceptionutils.errorHandler(var10);
      }

   }

   // $FF: synthetic method
   static final List listNotifications_aroundBody10(ExecutorIntegrationModuleV3Impl ajc$this, boolean readFlag, JoinPoint var2) {
      ApplicationConfig.getInstance().assertValidSession();

      try {
         byte[] sealedListNotificationsParam = ajc$this.getSealedListNotificationsParam(readFlag);
         ListNotificationsRequest request = new ListNotificationsRequest();
         request.setSecuredListNotificationsRequest(ajc$this.createSecuredContentType(sealedListNotificationsParam));
         ListNotificationsResponse response = null;

         try {
            response = RecipeExecutorServiceV3Impl.getInstance().listNotifications(request);
         } catch (ClientTransportException var10) {
            throw new IntegrationModuleException(I18nHelper.getLabel("error.connection.executor"), var10);
         }

         ajc$this.checkStatus(response);
         byte[] securedContent = response.getSecuredListNotificationsResponse().getSecuredContent();
         return ajc$this.createListNotificationItems(securedContent);
      } catch (Throwable var11) {
         Exceptionutils.errorHandler(var11);
         return null;
      }
   }

   // $FF: synthetic method
   static final void createFeedback_aroundBody12(ExecutorIntegrationModuleV3Impl ajc$this, String prescriberId, String rid, byte[] feedbackText, JoinPoint var4) {
      RidValidator.validateRid(rid);
      ApplicationConfig.getInstance().assertValidSession();

      try {
         ajc$this.getKmehrHelper().assertValidFeedback(feedbackText);
         List etkRecipients = ajc$this.getEtkHelper().getEtks(KgssIdentifierType.NIHII, prescriberId);

         for(int i = 0; i < etkRecipients.size(); ++i) {
            EncryptionToken etkRecipient = (EncryptionToken)etkRecipients.get(i);
            byte[] sealedCreateFeedbackParam = ajc$this.getSealedCreateFeedbackParam(feedbackText, etkRecipient, rid, prescriberId);
            CreateFeedbackRequest request = new CreateFeedbackRequest();
            request.setSecuredCreateFeedbackRequest(ajc$this.createSecuredContentType(sealedCreateFeedbackParam));
            ExecutorServiceAdministrativeInformationType info = ajc$this.getAdministrativeInfo(rid);
            info.setPrescriberIdentifier(ajc$this.createIdentifierType(prescriberId, IdentifierTypes.SSIN.name()));
            request.setAdministrativeInformation(info);

            try {
               ajc$this.checkStatus(RecipeExecutorServiceV3Impl.getInstance().createFeedback(request));
            } catch (ClientTransportException var18) {
               throw new IntegrationModuleException(I18nHelper.getLabel("error.connection.executor"), var18);
            }
         }
      } catch (Throwable var19) {
         Exceptionutils.errorHandler(var19);
      }

   }

   // $FF: synthetic method
   private static void ajc$preClinit() {
      Factory var0 = new Factory("ExecutorIntegrationModuleV3Impl.java", ExecutorIntegrationModuleV3Impl.class);
      ajc$tjp_0 = var0.makeSJP("method-execution", var0.makeMethodSig("1", "getPrescription", "be.business.connector.recipe.executor.ExecutorIntegrationModuleV3Impl", "java.lang.String", "rid", "be.business.connector.core.exceptions.IntegrationModuleException", "be.business.connector.recipe.executor.domain.GetPrescriptionForExecutorResult"), 65);
      ajc$tjp_1 = var0.makeSJP("method-execution", var0.makeMethodSig("1", "markAsArchived", "be.business.connector.recipe.executor.ExecutorIntegrationModuleV3Impl", "java.lang.String", "rid", "be.business.connector.core.exceptions.IntegrationModuleException", "void"), 133);
      ajc$tjp_2 = var0.makeSJP("method-execution", var0.makeMethodSig("1", "markAsDelivered", "be.business.connector.recipe.executor.ExecutorIntegrationModuleV3Impl", "java.lang.String", "rid", "be.business.connector.core.exceptions.IntegrationModuleException", "void"), 161);
      ajc$tjp_3 = var0.makeSJP("method-execution", var0.makeMethodSig("1", "markAsUndelivered", "be.business.connector.recipe.executor.ExecutorIntegrationModuleV3Impl", "java.lang.String", "rid", "be.business.connector.core.exceptions.IntegrationModuleException", "void"), 186);
      ajc$tjp_4 = var0.makeSJP("method-execution", var0.makeMethodSig("1", "revokePrescription", "be.business.connector.recipe.executor.ExecutorIntegrationModuleV3Impl", "java.lang.String:java.lang.String", "rid:reason", "be.business.connector.core.exceptions.IntegrationModuleException", "void"), 211);
      ajc$tjp_5 = var0.makeSJP("method-execution", var0.makeMethodSig("1", "listNotifications", "be.business.connector.recipe.executor.ExecutorIntegrationModuleV3Impl", "boolean", "readFlag", "be.business.connector.core.exceptions.IntegrationModuleException", "java.util.List"), 238);
      ajc$tjp_6 = var0.makeSJP("method-execution", var0.makeMethodSig("1", "createFeedback", "be.business.connector.recipe.executor.ExecutorIntegrationModuleV3Impl", "java.lang.String:java.lang.String:[B", "prescriberId:rid:feedbackText", "be.business.connector.core.exceptions.IntegrationModuleException", "void"), 268);
   }
}
