
# Zoom Connector for ConnID and Midpoint


## Overview

Open source Identity Management connector for [Zoom](https://zoom.us/) that uses the [ConnId](https://connid.tirasa.net/) framework.

This software also leverages the [Connector Base Framework](https://github.com/ExclamationLabs/connector-base). It is developed and tested with [Midpoint](https://evolveum.com/midpoint/), but also could be utilized with systems that implement the [ConnId](https://connid.tirasa.net/) framework.

This software is Copyright 2020-2024 Exclamation Graphics.  Licensed under the Apache License, Version 2.0.

Connector versions 4.0.1 and above support OAuth2 for authentication. Prior JWT authentication is no longer supported by Zoom.


## Features



* The connector configuration can be specified in the midPoint user interface or it can be read from a property file.
* The connector supports Zoom Meeting User, Zoom Phone Users, and Zoom Groups
* The connector can Create, Update, Delete, and search Zoom users.
* The connector can enable the Zoom Phone Feature for active Zoom Users.
* The connector can Assign or Unassign Calling Plans, Phone Numbers, Company Site, and Extension Number to an enabled Zoom Phone User account
* The connector can Create, Update, Delete, and List Zoom Groups.
* A User can be associated or disassociated with one or more Zoom Groups
* The list of Zoom Users can be searched by one of three statuses (active, inactive, pending)
* The connector will download users with all statuses by default unless a status is specified in the search criteria.
* The connector configuration will allow you to select whether a user to be deactivated, disassociated, or deleted when the Zoom User delete operation is invoked.


## Caveats



* Connector operation requires that you have at least a Zoom Pro Account. See [https://zoom.us/pricing](https://zoom.us/pricing) for more information.
* Basic Free Zoom accounts have rate limits which prevent normal operation of the connector. See [https://developers.zoom.us/docs/api/rest/rate-limits/](https://developers.zoom.us/docs/api/rest/rate-limits/) for more information.
* The connector currently supports the default create user action. We expect _custCreate_, _autoCreate_, and _ssoCreate_ to be available in a future release.
* The default create action is to set the user in pending status until activation is complete.
* The default create action requires the user to activate their zoom account by verifying their email address. Until activation is completed the account remains in 'pending' status.
* When a user is in a 'pending' status, the connector can use the API to lookup a user by ID or email address. However, the data returned is minimal or empty.
* A user’s information cannot be updated when the status is pending.
* A user’s information can be updated and retrieved when the status is active.
* A user’s information can be retrieved but not updated when the status is inactive
* A user’s information can only be updated when the account is activated or reactivated.


# Getting started

To begin you will need to register and activate at least a Zoom Pro account.  with one or more licenses for Zoom User and/or Zoom Phone. A normal Zoom account with zero licenses will not operate properly because of [rate limits](https://developers.zoom.us/docs/api/rest/rate-limits/).  \


You can manage Users and Groups in Zoom's web UI by going to [https://zoom.us/meeting](https://zoom.us/meeting) and using the Admin -> User Management links. The owner of the account or a user with **Admin** role is required to create the Server to Server OAuth application in the Zoom marketplace.

\
See [https://marketplace.zoom.us/develop](https://marketplace.zoom.us/develop) for information on setting up a Zoom developer account. Once you are authenticated, create a new [Server-Server OAuth](https://developers.zoom.us/docs/internal-apps/s2s-oauth/) app as documented here [https://developers.zoom.us/docs/internal-apps/create/](https://developers.zoom.us/docs/internal-apps/create/)


## Zoom Oauth2 Scopes

The following OAuth Scopes will be required in the Server to Server OAuth Application in order to use this connector.



* group:master
* group:read:admin,
* group:write:admin
* phone:master
* phone:read:admin
* phone:write:admin
* user:master
* user:read:admin
* user:write:admin


# Connector configuration

The actual method of configuring a connector is largely dependent on the interface(s) provided by your Identity and Access management system. Midpoint provides a convenient user interface method to enter these values. If configuration properties are being read in from a property file you may also need to know the name of the property.


## Configuration Parameters

The configuration parameters are specified in the following table. One thing to watch out for is the setting for the OAuth2 client secret when using the midpoint interface. You should collapse the configuration


<table>
  <tr>
   <td><strong>Item</strong>
   </td>
   <td><strong>Req’d</strong>
   </td>
   <td><strong>Description</strong>
   </td>
  </tr>
  <tr>
   <td>Service URL
   </td>
   <td>Yes
   </td>
   <td>The base URL of the Zoom Web Service. It is normally set to <strong><em>https://api.zoom.us/v2</em></strong>
   </td>
  </tr>
  <tr>
   <td>IO Error Retries
   </td>
   <td>No
   </td>
   <td>Number of retries that will be attempted when an IO error occurs. Default is 5.
   </td>
  </tr>
  <tr>
   <td>Deep Get Enabled
   </td>
   <td>No
   </td>
   <td>When a search operation is executed and this value is <strong>true,</strong> the connector will download all attributes for each individual record returned. When <strong>false</strong> the Zoom connector will not return Zoom phone attributes. The value should be set to true although it will consume more time to download each record.
   </td>
  </tr>
  <tr>
   <td>Deep Import Enabled
   </td>
   <td>No
   </td>
   <td>When an import operation is executed and this value is <strong>true</strong> the connector will download all attributes for each individual record returned. When <strong>false</strong> the Zoom connector will not return Zoom phone attributes. The value should be set to true although it will consume more time to download each record. 
   </td>
  </tr>
  <tr>
   <td>Import Batch Size
   </td>
   <td>No
   </td>
   <td>The default number of records to retrieve per page. Import operations will be invoked using the given batch size when it is supplied. Since the Zoom API supports paging you can import records one batch/page at a time instead of all at once. The default value is 30 and the maximum value is 300.
   </td>
  </tr>
  <tr>
   <td>Pagination Enabled
   </td>
   <td>No
   </td>
   <td>The zoom Connector supports pagination on supported objects. This option should be set to true.
   </td>
  </tr>
  <tr>
   <td>Duplicate Record Returns Id
   </td>
   <td>No
   </td>
   <td>When a create is attempted and an AlreadyExistsException is generated by the driver invocator, the adapter shall attempt to return the id of the existing record matching the specified email address. 
   </td>
  </tr>
  <tr>
   <td>OAuth2 Token URL
   </td>
   <td>Yes
   </td>
   <td>The URL used to get an OAUTH2 token. The default value for Zoom is <strong><em>https://zoom.us/oauth/token</em></strong>
   </td>
  </tr>
  <tr>
   <td>OAuth2 Account Id
   </td>
   <td>Yes
   </td>
   <td>The <strong><em>Account ID</em></strong> received from the Server to Server OAuth2 application you created in the Zoom Marketplace.
   </td>
  </tr>
  <tr>
   <td>OAuth2 Client Id
   </td>
   <td>Yes
   </td>
   <td>The <strong><em>Client Id</em></strong> received from the App Credentials page of the Server to Server OAuth2 application you created in the Zoom Marketplace.
   </td>
  </tr>
  <tr>
   <td>OAuth2 Client Secret
   </td>
   <td>Yes
   </td>
   <td>The <strong><em>Client Secret</em></strong> received from the App Credentials page of the Server to Server OAuth2 application you created in the Zoom Marketplace.
   </td>
  </tr>
  <tr>
   <td>OAuth2 Scope
   </td>
   <td>No
   </td>
   <td>The <strong><em>Scopes</em></strong> assigned to the Server to Server OAuth2 application you created in the Zoom Marketplace. As of this writing the Zoom system will provide all the scopes defined for the application by default. For this reason it may be possible to leave this value blank or poplate with the values supplied in a previous section of this document.
   </td>
  </tr>
  <tr>
   <td>Deactivate On Delete
   </td>
   <td>No
   </td>
   <td>When this value is <strong><em>true</em></strong> the connector will deactivate the account when a delete operation is invoked. When this value is <strong>false</strong> the <em>Disassociate on Delete</em> action is invoked.
   </td>
  </tr>
  <tr>
   <td>Disassociate On Delete
   </td>
   <td>No
   </td>
   <td>When this value is <strong>true</strong> the connector will disassociate the user’s zoom login from the master account. This means the user login with the user’s email address will continue to exist as a basic account. When this value is <strong>false</strong> the user account is actually deleted unless <em>Deactivate on Delete</em> is true.
   </td>
  </tr>
  <tr>
   <td>Immediate Logout on Deactivate
   </td>
   <td>No
   </td>
   <td>When this value is true the user will be immediately logged out when a deactivate event occurs. 
   </td>
  </tr>
</table>



## Configuration properties

The following property names can be used when integrating with a Connid system that uses a configuration properties file. It is also used to perform unit tests in the code base.  \
See src/test/resources/__bcon__development__exclamation_labs__zoom.properties for an example.


<table>
  <tr>
   <td><strong>Item</strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Property Name</strong>
   </td>
  </tr>
  <tr>
   <td>Service URL
   </td>
   <td>String
   </td>
   <td>service.serviceUrl
   </td>
  </tr>
  <tr>
   <td>IO Error Retries
   </td>
   <td>Integer
   </td>
   <td>rest.ioErrorRetries
   </td>
  </tr>
  <tr>
   <td>Deep Get Enabled
   </td>
   <td>Boolean
   </td>
   <td>results.deepGet
   </td>
  </tr>
  <tr>
   <td>Deep Import Enabled
   </td>
   <td>Boolean
   </td>
   <td>results.deepImport
   </td>
  </tr>
  <tr>
   <td>Import Batch Size
   </td>
   <td>Integer
   </td>
   <td>results.importBatchSize
   </td>
  </tr>
  <tr>
   <td>Pagination Enabled
   </td>
   <td>Boolean
   </td>
   <td>results.pagination
   </td>
  </tr>
  <tr>
   <td>Duplicate Record Returns Id
   </td>
   <td>Boolean
   </td>
   <td>service.duplicateErrorReturnsId
   </td>
  </tr>
  <tr>
   <td>OAuth2 Token URL
   </td>
   <td>String
   </td>
   <td>security.authenticator.oauth2ClientCredentials.tokenUrl
   </td>
  </tr>
  <tr>
   <td>OAuth2 Account Id
   </td>
   <td>String
   </td>
   <td>custom.accountId
   </td>
  </tr>
  <tr>
   <td>OAuth2 Client Id
   </td>
   <td>String
   </td>
   <td>security.authenticator.oauth2ClientCredentials.clientId
   </td>
  </tr>
  <tr>
   <td>OAuth2 Client Secret
   </td>
   <td>String
   </td>
   <td>security.authenticator.oauth2ClientCredentials.clientSecret
   </td>
  </tr>
  <tr>
   <td>OAuth2 Scope
   </td>
   <td>String
   </td>
   <td>security.authenticator.oauth2ClientCredentials.scope
   </td>
  </tr>
  <tr>
   <td>Deactivate On Delete
   </td>
   <td>Boolean
   </td>
   <td>custom.deactivateOnDelete
   </td>
  </tr>
  <tr>
   <td>Disassociate On Delete
   </td>
   <td>Boolean
   </td>
   <td>custom.disassociateOnDelete
   </td>
  </tr>
  <tr>
   <td>Immediate Logout on Deactivate
   </td>
   <td>Boolean
   </td>
   <td>custom.immediateLogoutOnDeactivate 
   </td>
  </tr>
</table>



## Connector Schema

The connector schema is drawn from available variables in the Zoom User API and the Zoom Phone API. it is


<table>
  <tr>
   <td><strong>Attribute </strong>
   </td>
   <td><strong>Type</strong>
   </td>
   <td><strong>Comment</strong>
   </td>
  </tr>
  <tr>
   <td>USER_ID
   </td>
   <td>String
   </td>
   <td>Zoom User ID
   </td>
  </tr>
  <tr>
   <td>EMAIL
   </td>
   <td>String
   </td>
   <td>The user’s email address 
   </td>
  </tr>
  <tr>
   <td>FIRST_NAME
   </td>
   <td>String
   </td>
   <td>The user's first name.
   </td>
  </tr>
  <tr>
   <td>LAST_NAME
   </td>
   <td>String
   </td>
   <td>The user's last name.
   </td>
  </tr>
  <tr>
   <td>LANGUAGE
   </td>
   <td>String
   </td>
   <td>Default language for the Zoom Web Portal.
   </td>
  </tr>
  <tr>
   <td>TIME_ZONE
   </td>
   <td>String
   </td>
   <td>The User’s timezone assignments
   </td>
  </tr>
  <tr>
   <td>PHONE_NUMBER
   </td>
   <td>String
   </td>
   <td>The user’s phone number. The value is arbitrary and not necessarily a zoom phone number. 
   </td>
  </tr>
  <tr>
   <td>PHONE_COUNTRY
   </td>
   <td>String
   </td>
   <td>The country of the user’s phone number
   </td>
  </tr>
  <tr>
   <td>CREATED_AT
   </td>
   <td>String
   </td>
   <td>The date and time when this user was created
   </td>
  </tr>
  <tr>
   <td>LAST_LOGIN_TIME
   </td>
   <td>String
   </td>
   <td>The date and time when the user last logged into Zoom.
   </td>
  </tr>
  <tr>
   <td>TYPE
   </td>
   <td>Integer
   </td>
   <td>The plan type of user.  \
1 - Basic.
<p>
2 - Licensed.
<p>
99 - None \
A user does not need to be licensed in order to have a zoom phone.
   </td>
  </tr>
  <tr>
   <td>GROUP_IDS
   </td>
   <td>[String]
   </td>
   <td>An Array or Group IDs associated with the User
   </td>
  </tr>
  <tr>
   <td>CREATED_AT
   </td>
   <td>String
   </td>
   <td>The date and time when this user's latest login type was created. 
   </td>
  </tr>
  <tr>
   <td>VERIFIED
   </td>
   <td>String
   </td>
   <td>Specified whether the user is verified or not. The value is 1 when the user is verified
   </td>
  </tr>
  <tr>
   <td>STATUS
   </td>
   <td>String
   </td>
   <td>The Zoom User status can contain the values “<strong>active</strong>”, “<strong>inactive</strong>”, or “<strong>pending</strong>”
   </td>
  </tr>
  <tr>
   <td>PERSON0AL_MEETING_ID
   </td>
   <td>Integer
   </td>
   <td>The Zoom User’s personal Meeting id or pmi
   </td>
  </tr>
  <tr>
   <td>ZOOM_PHONE_FEATURE
   </td>
   <td>Boolean
   </td>
   <td>When this value is true the Zoom phone feature is enabled. 
   </td>
  </tr>
  <tr>
   <td>ZOOM_ONE_FEATURE_TYPE
   </td>
   <td>String
   </td>
   <td>The Zoom User’s Zoom workplace plan option. This value is available at user creation time. The connector does not allow for the value to be changed once set.
   </td>
  </tr>
  <tr>
   <td>SITE_ID
   </td>
   <td> String
   </td>
   <td>The site ID is the unique identifier of the site associated with the zoom phone assigned to the user. The connector does not create or update Zoom Sites. This is a manual operation for the Administrator of the Zoom License.
   </td>
  </tr>
  <tr>
   <td>SITE_NAME
   </td>
   <td>String
   </td>
   <td>The name of the site associated with the Zoom Phone assigned to the user. When creating a user who has a zoom phone license only the site name is required. 
   </td>
  </tr>
  <tr>
   <td>SITE_CODE
   </td>
   <td>Integer
   </td>
   <td>The identifier for the site associated with the zoom phone assigned to the user.
   </td>
  </tr>
  <tr>
   <td>ZOOM_PHONE_STATUS
   </td>
   <td>String
   </td>
   <td>The status of the user's Zoom Phone license. When the value is “<strong>activate”,</strong> the Zoom phone is active. When the value is “<strong>deactivate”, </strong>the user’s Zoom phone license is disabled. When the status is deactivated, the user can't make or receive calls
   </td>
  </tr>
  <tr>
   <td>EXTENSION_NUMBER
   </td>
   <td>String
   </td>
   <td>The extension number assigned to the user's Zoom phone number. The extension number is associated with the Site where the user is located. 
   </td>
  </tr>
  <tr>
   <td>ZOOM_PHONE_CALLING_PLANS
   </td>
   <td>[Integer]
   </td>
   <td>An array of Zero or more calling plans defined by the Zoom Phone API. A link to the list of calling plan values is provided in the references section.
   </td>
  </tr>
  <tr>
   <td>ZOOM_PHONE_NUMBERS
   </td>
   <td>[String]
   </td>
   <td>The list of zero or more phone numbers assigned to the user. The phone number(s) assigned to the user must have been obtained from a zoom phone license. The connector will not assign a phone number to a user who does not have the zoom phone feature enabled.
   </td>
  </tr>
</table>



# Connector Operations

The Zoom connector implements the following connId SPI operations:



* **SchemaOp** - Allows the Connector to describe which types of objects the Connector manages on the target resource. This includes the options supported for each type of object.
* **TestOp** - Allows testing of the resource configuration to verify that the target environment is available.
* **SearchOp** - Allows the connector to search the Zoom Web Service for resource objects.
* **CreateOp** - Allows the connector to create Users or Groups
* **DeleteOp** - Allows the connector to delete Users, or Groups
* **UpdateDeltaOp** - Allows for updates of the supported Object Types. These are Users and Groups


## Deep Get Explained

The connector supports a **deep get** functionality which returns detailed information for each item returned from a query. This feature is necessary because a query may return partial fields for a record.This is the case with the Zoom User lookup and the lookup API calls. **Deep get** is invoked when the query contains paging parameters such as page size and page offset. **Deep get is recommended to be true for this connector.**


## Deep Import Explained

The connector’s deep import option is similar to the deep get option. The deep import option is invoked when a query does not have paging parameters. **Deep Import is recommended to be true for this connector.**


## Duplicate Record Returns Id Explained

The duplicate record returns Id configuration option is invoked when an HTTP POST request, used to create a record, returns HTTP 409 (Conflict). This typically indicates that the record we are attempting to create already exists. When this option is true the connector will attempt to get the record by name and return the record’s ID value to the caller. In this way a record can be seamlessly imported when it already exists on the target server. Unfortunately the Zoom API does not return HTTP 409 instead it returns HTTP 412. Because this is the case the connector will always do a lookup for an existing object type before creating the type.

## Email Address Changes

The Zoom API does not allow an email address to be changed once the user is created unless you have Managed Domains enabled. 
You enable this through Account Management. The connector will fail gracefully with an error message in the log. 
If more than 3 attempts are made to change a user's address within a 24 hour period, the Zoom API will return a rate limiting error (HTTP 429). 

# References



1. [https://developers.zoom.us/docs/api/rest/reference/user/methods/#overview](https://developers.zoom.us/docs/api/rest/reference/user/methods/#overview)
2. [https://developers.zoom.us/docs/api/rest/reference/phone/methods/#overview](https://developers.zoom.us/docs/api/rest/reference/phone/methods/#overview)
3. [Zoom User API](https://developers.zoom.us/docs/api/rest/reference/user/methods/#tag/Users)
4. [Zoom Phone User API](https://developers.zoom.us/docs/api/rest/reference/phone/methods/#tag/Users)
5. [Zoom Site API ](https://developers.zoom.us/docs/zoom-phone/apis/#tag/Sites)
6. [List of Zoom Phone Calling Plans](https://developers.zoom.us/docs/api/rest/other-references/calling-plans/)