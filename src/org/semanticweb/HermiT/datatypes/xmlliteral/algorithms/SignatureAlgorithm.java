/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.semanticweb.HermiT.datatypes.xmlliteral.algorithms;


import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;

import org.semanticweb.HermiT.datatypes.xmlliteral.algorithms.implementations.IntegrityHmac;
import org.semanticweb.HermiT.datatypes.xmlliteral.exceptions.AlgorithmAlreadyRegisteredException;
import org.semanticweb.HermiT.datatypes.xmlliteral.exceptions.XMLSecurityException;
import org.semanticweb.HermiT.datatypes.xmlliteral.signature.XMLSignatureException;
import org.semanticweb.HermiT.datatypes.xmlliteral.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Allows selection of digital signature's algorithm, private keys, other security parameters, and algorithm's ID.
 *
 * @author Christian Geuer-Pollmann
 */
public class SignatureAlgorithm extends Algorithm {

   /** Field _alreadyInitialized */
   static boolean _alreadyInitialized = false;

   /** All available algorithm classes are registered here */
   @SuppressWarnings("unchecked")
static HashMap _algorithmHash = null;

   /** Field _signatureAlgorithm */
   protected SignatureAlgorithmSpi _signatureAlgorithm = null;

   /**
    * Constructor SignatureAlgorithm
    *
    * @param doc
    * @param algorithmURI
    * @throws XMLSecurityException
    */
   @SuppressWarnings("unchecked")
public SignatureAlgorithm(Document doc, String algorithmURI)
           throws XMLSecurityException {

      super(doc, algorithmURI);

      try {
         Class implementingClass =
            SignatureAlgorithm.getImplementingClass(algorithmURI);
         if (true)
         	if (log.isLoggable(java.util.logging.Level.FINE))                                     log.log(java.util.logging.Level.FINE, "Create URI \"" + algorithmURI + "\" class \""
                   + implementingClass + "\"");

         this._signatureAlgorithm =
            (SignatureAlgorithmSpi) implementingClass.newInstance();
      }  catch (IllegalAccessException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };

         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      } catch (InstantiationException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };

         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      } catch (NullPointerException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };

         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      }
   }

   /**
    * Constructor SignatureAlgorithm
    *
    * @param doc
    * @param algorithmURI
    * @param HMACOutputLength
    * @throws XMLSecurityException
    */
   public SignatureAlgorithm(
           Document doc, String algorithmURI, int HMACOutputLength)
              throws XMLSecurityException {

      this(doc, algorithmURI);

      this._signatureAlgorithm.engineSetHMACOutputLength(HMACOutputLength);
      ((IntegrityHmac)this._signatureAlgorithm)
         .engineAddContextToElement(this._constructionElement);
   }

   /**
    * Constructor SignatureAlgorithm
    *
    * @param element
    * @param BaseURI
    * @throws XMLSecurityException
    */
   @SuppressWarnings("unchecked")
public SignatureAlgorithm(Element element, String BaseURI)
           throws XMLSecurityException {

      super(element, BaseURI);

      String algorithmURI = this.getURI();

      try {
         Class implementingClass =
            SignatureAlgorithm.getImplementingClass(algorithmURI);
         if (true)
         	if (log.isLoggable(java.util.logging.Level.FINE))                                     log.log(java.util.logging.Level.FINE, "Create URI \"" + algorithmURI + "\" class \""
                   + implementingClass + "\"");

         this._signatureAlgorithm =
            (SignatureAlgorithmSpi) implementingClass.newInstance();

         this._signatureAlgorithm
            .engineGetContextFromElement(this._constructionElement);
      }  catch (IllegalAccessException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };

         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      } catch (InstantiationException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };

         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      } catch (NullPointerException ex) {
         Object exArgs[] = { algorithmURI, ex.getMessage() };

         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
                                         ex);
      }
   }

   public byte[] sign() throws XMLSignatureException {
      return this._signatureAlgorithm.engineSign();
   }

   public String getJCEAlgorithmString() {
      return this._signatureAlgorithm.engineGetJCEAlgorithmString();
   }

   /**
    * Method getJCEProviderName
    *
    * @return The Provider of this Signature Alogrithm
    */
   public String getJCEProviderName() {
      return this._signatureAlgorithm.engineGetJCEProviderName();
   }

   public void update(byte[] input) throws XMLSignatureException {
      this._signatureAlgorithm.engineUpdate(input);
   }

   public void update(byte input) throws XMLSignatureException {
      this._signatureAlgorithm.engineUpdate(input);
   }

   public void update(byte buf[], int offset, int len)
           throws XMLSignatureException {
      this._signatureAlgorithm.engineUpdate(buf, offset, len);
   }

   public void initSign(Key signingKey) throws XMLSignatureException {
      this._signatureAlgorithm.engineInitSign(signingKey);
   }

   public void initSign(Key signingKey, SecureRandom secureRandom)
           throws XMLSignatureException {
      this._signatureAlgorithm.engineInitSign(signingKey, secureRandom);
   }

   public void initSign(
           Key signingKey, AlgorithmParameterSpec algorithmParameterSpec)
              throws XMLSignatureException {
      this._signatureAlgorithm.engineInitSign(signingKey,
                                              algorithmParameterSpec);
   }
   public void setParameter(AlgorithmParameterSpec params)
           throws XMLSignatureException {
      this._signatureAlgorithm.engineSetParameter(params);
   }

   public void initVerify(Key verificationKey) throws XMLSignatureException {
      this._signatureAlgorithm.engineInitVerify(verificationKey);
   }

   public boolean verify(byte[] signature) throws XMLSignatureException {
      return this._signatureAlgorithm.engineVerify(signature);
   }

   /**
    * Returns the URI representation of Transformation algorithm
    *
    * @return the URI representation of Transformation algorithm
    */
   public final String getURI() {
      return this._constructionElement.getAttributeNS(null,
              Constants._ATT_ALGORITHM);
   }


   @SuppressWarnings("unchecked")
public static void providerInit() {

      if (SignatureAlgorithm.log == null) {
         SignatureAlgorithm.log =
            java.util.logging.Logger
               .getLogger(SignatureAlgorithm.class.getName());
      }

      if (log.isLoggable(java.util.logging.Level.FINE))                                     log.log(java.util.logging.Level.FINE, "Init() called");

      if (!SignatureAlgorithm._alreadyInitialized) {
         SignatureAlgorithm._algorithmHash = new HashMap(10);
         SignatureAlgorithm._alreadyInitialized = true;
      }
   }


   @SuppressWarnings("unchecked")
public static void register(String algorithmURI, String implementingClass)
           throws AlgorithmAlreadyRegisteredException,XMLSignatureException {

      {
         if (true)
         	if (log.isLoggable(java.util.logging.Level.FINE))                                     log.log(java.util.logging.Level.FINE, "Try to register " + algorithmURI + " " + implementingClass);

         // are we already registered?
         Class registeredClassClass =
            SignatureAlgorithm.getImplementingClass(algorithmURI);
		 if (registeredClassClass!=null) {
			 String registeredClass = registeredClassClass.getName();

			 if ((registeredClass != null) && (registeredClass.length() != 0)) {
				 Object exArgs[] = { algorithmURI, registeredClass };

				 throw new AlgorithmAlreadyRegisteredException(
						 "algorithm.alreadyRegistered", exArgs);
			 }
		 }
		 try {	         	   			 
			 SignatureAlgorithm._algorithmHash.put(algorithmURI, Class.forName(implementingClass));
	      } catch (ClassNotFoundException ex) {
	         Object exArgs[] = { algorithmURI, ex.getMessage() };

	         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
	                                         ex);
	      } catch (NullPointerException ex) {
	         Object exArgs[] = { algorithmURI, ex.getMessage() };

	         throw new XMLSignatureException("algorithms.NoSuchAlgorithm", exArgs,
	                                         ex);
	      }
         
      }
   }

   /**
    * Method getImplementingClass
    *
    * @param URI
    * @return the class that implements the URI
    */
   @SuppressWarnings("unchecked")
private static Class getImplementingClass(String URI) {

      if (SignatureAlgorithm._algorithmHash == null) {
         return null;
      }

      return (Class) SignatureAlgorithm._algorithmHash.get(URI);
   }

   /**
    * Method getBaseNamespace
    *
    * @return URI of this element
    */
   public String getBaseNamespace() {
      return Constants.SignatureSpecNS;
   }

   /**
    * Method getBaseLocalName
    *
    * @return Local name
    */
   public String getBaseLocalName() {
      return Constants._TAG_SIGNATUREMETHOD;
   }
}
