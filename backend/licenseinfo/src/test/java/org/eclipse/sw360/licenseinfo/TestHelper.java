/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.licenseinfo;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentType;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import org.eclipse.sw360.licenseinfo.parsers.AttachmentContentProvider;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author: maximilian.huber@tngtech.com
 */
public class TestHelper {

    public static InputStream makeAttachmentContentStream(String filename){
        return TestHelper.class.getClassLoader().getResourceAsStream(filename);
    }

    public static AttachmentContent makeAttachmentContent(String filename){
        return new AttachmentContent()
                .setId(filename)
                .setFilename(filename);
    }

    public static Attachment makeAttachment(String filename, AttachmentType attachmentType){
        return new Attachment()
                .setAttachmentContentId(filename)
                .setFilename(filename)
                .setAttachmentType(attachmentType);
    }

    //==================================================================================================================
    // AttachmentContentStore:
    public static class AttachmentContentStore{
        private Map<String,AttachmentContent> store;
        private AttachmentConnector connectorMock;

        public AttachmentContentStore(AttachmentConnector connectorMock){
            store = new HashMap<>();
            this.connectorMock = connectorMock;
        }

        public AttachmentContentProvider getAttachmentContentProvider(){
            return this::get;
        }

        public AttachmentContent get(Attachment attachment){
            return get(attachment.getAttachmentContentId());
        }

        public AttachmentContent get(String attachmentContentId){
            return store.get(attachmentContentId);
        }

        public AttachmentContentStore put(String filename, String fileContent) throws TException, IOException {
            AttachmentContent attachmentContent = makeAttachmentContent(filename);
            store.put(attachmentContent.getId(), attachmentContent);
            when(connectorMock.getAttachmentStream(eq(attachmentContent), any(), any())).thenReturn(
                    ReaderInputStream.builder().setReader(new StringReader(fileContent)).get()
            );
            return this;
        }

        public AttachmentContentStore put(String filename) throws TException {
            return put(makeAttachmentContent(filename));
        }

        public AttachmentContentStore put(AttachmentContent attachmentContent) throws TException {
            store.put(attachmentContent.getId(), attachmentContent);
            when(connectorMock.getAttachmentStream(eq(attachmentContent), any(), any())).thenReturn(makeAttachmentContentStream(attachmentContent.getFilename()));
            return this;
        }
    }

    //==================================================================================================================
    // Assertions:
    public static void assertLicenseInfo(LicenseInfo info){
        assertLicenseInfo(info, true);
    }

    public static void assertLicenseInfo(LicenseInfo info, boolean assertNonempty) {
        Assert.assertNotNull(info.getFilenames());
        Assert.assertFalse(info.getFilenames().isEmpty());

        if(assertNonempty){
            Assert.assertNotNull(info.getCopyrights());
            Assert.assertNotNull(info.getLicenseNamesWithTexts());
            Assert.assertTrue(info.getLicenseNamesWithTexts().stream()
                    .filter(lt -> lt.isSetLicenseText())
                    .findAny()
                    .isPresent());
        }
    }

    public static void assertLicenseInfoParsingResult(LicenseInfoParsingResult result){
        assertLicenseInfoParsingResult(result, LicenseInfoRequestStatus.SUCCESS);
    }

    public static void assertLicenseInfoParsingResult(LicenseInfoParsingResult result, LicenseInfoRequestStatus status){
        Assert.assertEquals(status, result.getStatus());
        Assert.assertNotNull(result.getLicenseInfo());
        assertLicenseInfo(result.getLicenseInfo(), status == LicenseInfoRequestStatus.SUCCESS);
    }
}
