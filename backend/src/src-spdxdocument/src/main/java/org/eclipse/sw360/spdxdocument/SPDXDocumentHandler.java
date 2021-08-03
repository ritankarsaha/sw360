/*
 * Copyright Toshiba corporation, 2021. Part of the SW360 Portal Project.
 * Copyright Toshiba Software Development (Vietnam) Co., Ltd., 2021. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.spdxdocument;

import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.AddDocumentRequestSummary;
import org.eclipse.sw360.datahandler.thrift.spdxdocument.*;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.spdxdocument.db.*;

import com.cloudant.client.api.CloudantClient;

import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Supplier;

import static org.eclipse.sw360.datahandler.common.SW360Assert.*;

/**
 * Implementation of the Thrift service
 *
 * @author hieu1.phamvan@toshiba.co.jp
 */
public class SPDXDocumentHandler implements SPDXDocumentService.Iface {

    SpdxDocumentDatabaseHandler handler;

    SPDXDocumentHandler() throws MalformedURLException {
        handler = new SpdxDocumentDatabaseHandler(DatabaseSettings.getConfiguredClient(), DatabaseSettings.COUCH_DB_SPDX);
    }

    SPDXDocumentHandler(Supplier<CloudantClient> httpClient, String dbName) throws MalformedURLException {
        handler = new SpdxDocumentDatabaseHandler(httpClient, dbName);
    }

    @Override
    public List<SPDXDocument> getSPDXDocumentSummary(User user) throws TException {
        return handler.getSPDXDocumentSummary(user);
    }

    @Override
    public SPDXDocument getSPDXDocumentById(String id, User user) throws TException {
        return handler.getSPDXDocumentById(id, user);
    }

    @Override
    public AddDocumentRequestSummary addSPDXDocument(SPDXDocument spdx, User user) throws TException {
        return handler.addSPDXDocument(spdx, user);
    }

    @Override
    public RequestStatus updateSPDXDocument(SPDXDocument spdx, User user) throws TException {
        return handler.updateSPDXDocument(spdx, user);
    }

    @Override
    public RequestStatus deleteSPDXDocument(String id, User user) throws TException {
        assertId(id);
        assertUser(user);

        return handler.deleteSPDXDocument(id, user);
    }

}