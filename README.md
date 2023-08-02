# Zoom Connector

Open source Identity Management connector for [Zoom](https://zoom.us)

Leverages the [Connector Base Framework](https://github.com/ExclamationLabs/connector-base)

Developed and tested in [Midpoint](https://evolveum.com/midpoint/), but also could be utilized in any [ConnId](https://connid.tirasa.net/) framework. 

## Introductory Notes

- This software is Copyright 2020 Exclamation Labs.  Licensed under the Apache License, Version 2.0.

- As of this writing, there is no public Java API for Zoom User and Group management.

- Connector versions 4.0.1 and above now support OAuth2 for authentication.  Prior JWT authentication is no longer supported by Zoom.

- Limitations:
 
    - You can create a Zoom developer account to access the API, but you won't be successful
    in creating Groups of users unless you have paid for a non-Basic (non-free)
     account with Zoom.  See <https://zoom.us/pricing> for more information.     

    - When creating a user, the user must verify his email address and move from 'pending' to 'active'
 status before he will actually be returned in a list of Zoom users or be assigned to groups.
  
    - When a user is in a 'pending' status, the API only returns the user's id.  None of the other information is returend.
  
    - Because of this manual user verification step, it is not possible for a user to be assigned to group(s) while
    it is being created.  You can only add and remove groups from an 'active' user.
 
    - The connector has only been tested with 'Basic' users in Zoom and normal user
    creation (other types are autoCreate, custCreate and ssoCreate).  Some of these
    types cannot be used unless you have a particular type of Zoom account.

    - If a Zoom user is deactivated, it's information cannot be updated in any other way until/when 
  it has been reactivated.
  
    - Per API, list of Zoom users must be obtained by one of three statuses (active, inactive, pending) - 
  https://developers.zoom.us/docs/api/rest/reference/zoom-api/methods/#operation/users ... for this reason
  the invocator needs to make three separate requests.  Therefore, Zoom does not support pagination natively
  to return the correct results.

## Getting started
See <https://marketplace.zoom.us/develop> for information on setting up a Zoom developer
 account. Once you are authenticated, create a new Server-Server OAuth app
as documented here: <https://marketplace.zoom.us/docs/guides/build/server-to-server-oauth-app/>.

Once this is all done, you can manage Users and Groups in Zoom's web UI by going to
<https://zoom.us/meeting> and using the Admin -> User Management links.

## Midpoint configuration

See XML files in src/test/resources folder for Midpoint examples.  resourceOverlay.xml is an example
resource configuration setup for Midpoint.

## Configuration properties 

- See src/test/resources/__bcon__development__exclamation_labs__zoom.properties for an example

- service.serviceUrl - Normally set to `https://api.zoom.us/v2`

- security.authenticator.oauth2ClientCredentials.tokenUrl - Normally set to `https://zoom.us/oauth/token`
 
- security.authenticator.oauth2ClientCredentials.clientId - Obtained while managing Server-Server OAuth app

- security.authenticator.oauth2ClientCredentials.clientSecret - Obtained while managing Server-Server OAuth app

- security.authenticator.oauth2ClientCredentials.scope - Scopes seem to be recognized automatically, 
but just in case this can be used: `/group:read:admin,/group:write:admin,/user:master,/user:write:admin`

- custom.accountId - Obtained while managing Server-Server OAuth app
 
- results.pagination (optional) - This should be set to `true` to support Zoom pagination for getting all users.

- results.deepGet (optional) - Not needed for Zoom, should be false.

- results.deepImport (optional) - Not needed for Zoom, should be false.

- results.importBatchSize (optional) - Not needed for Zoom, should be false.

- rest.ioErrorRetries (optional, default is 5) - the number of times an API invocation will be retried before giving up.


