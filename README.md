# Authorization Server with Spring Security

This demo app explores nuances of implementing OAuth2.0 and OIDC protocols using Spring security. 
Takes a slight detour from the standard OAuth2.0 recommended flow and shines light on following aspects just to have a closer look at how
the wiring works across Authorization server, Resource server, and the Third party ID provider, 
the verification of ID token and Access tokens behind the scenese.

**Following aspects are covered in this demo project:**
*  Validating third-party authenticated users through ID token verification.
*  Mapping users to appropriate authorities based on retrieved information.
*  Issuing access tokens to authorized users.
*  Securing endpoints on the resource server and automatically verifying access tokens.

Refer blog post here
https://infinitesimalpoints.blogspot.com/2024/02/spring-security-with-oauth-20-and.html

## How to use Postman to call Authorization server

First Use the Authorization tab, supply Google registered client-id and client-secret and obtain an ID token.

Make a POST call to the Authorization Server's token endpoint to obtain an Access Token.

Example:
curl --location --request POST 'http://localhost:8081/validate-google-token?id_token=< _ID token_>' \
--header 'Authorization: ya29.a0AfB_byA-RznBfFXekofUhiOtxZlizcgmKGImm81XmZ-d40d623NGemzZw-ICaWQXUS-wx-saP7Hm4Vt9OxCWlfpwXVUg3wqPg-AB_D5u_Z2ljuOasS1DiKfLnslJ4MwTAmP78-nppB239qiCLfsI0VDwhXqVRMwPwb8aCgYKAZsSARASFQHGX2MiqXiF1wIreZEyHMfrYL7peQ0170' \
--header 'Cookie: JSESSIONID=0B84A8527F92BAE7CBCFCDD1369C3D24'

Copy the access token and include it in the Resource Server endpoint's request header as the Bearer token.

curl --location 'http://localhost:8080/api/v1/hello/authtest' \
--header 'Authorization: Bearer Token' \
--header 'Cookie: JSESSIONID=0B84A8527F92BAE7CBCFCDD1369C3D24'

If the Authentication and the Authorization is successful you should get HTTP 200 along with the response message from the Resource server. 