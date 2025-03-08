# blas-idp

> OAuth2
>> Login with OAuth 2.0: http://localhost:8080/login <br/>
> > Login endpoint for Google: http://localhost:8080/oauth2/authorization/google <br/>
> > Login endpoint for Facebook: http://localhost:8080/oauth2/authorization/facebook

## Changes

| Version | Date release | Tickets/Notes                                           |
|---------|--------------|---------------------------------------------------------|
| 3.7.0   | 06/12/2024   | Mask sensitive http.url span attribute                  |
| 3.6.0   | 05/12/2024   | Add OpenTelemetry agent extension                       |
| 3.5.0   | 29/11/2024   | Support OpenTelemetry log data model and trace          |
| 3.4.0   | 14/11/2024   | Uptake blas-common 6.3.0                                |
| 3.3.0   | 08/11/2024   | Uptake blas-bom 1.23.0 and blas-common 6.2.0            |
| 3.2.0   | 31/10/2024   | Uptake blas-common 6.0.0                                |
| 3.1.0   | 05/10/2024   | Add multiple namespace on Kubernetes                    |
| 3.0.0   | 13/09/2024   | Integrate with Hashicorp Vault                          |
| 2.17.0  | 03/09/2024   | Collect telemetry data using OpenTelemetry              |
| 2.16.0  | 29/08/2024   | Change to use Blas JDK base image                       |
| 2.15.0  | 15/08/2024   | Uptake blas-bom 1.21.0 and blas-common 5.15.0           |
| 2.14.0  | 02/08/2024   | Uptake blas-common v5.14.0                              |
| 2.13.0  | 17/06/2024   | Show plain text jwt in URL                              |
| 2.12.0  | 14/06/2024   | Add MongoDB config                                      |
| 2.11.0  | 21/06/2024   | Support change password and user information            |
| 2.10.0  | 24/05/2024   | Support Global ID to trace the request                  |
| 2.9.0   | 05/04/2024   | Uptake blas-bom 1.18.0 and blas-common 5.3.0            |
| 2.8.0   | 23/03/2024   | Upgrade Java 21                                         |
| 2.7.0   | 23/03/2024   | Uptake blas-common 5.1.0                                |
| 2.6.0   | 18/03/2024   | Uptake blas-bom 1.16.0 and blas-common 5.0.0            |
| 2.5.0   | 01/01/2024   | Add time zone configuration and PDF password            |
| 2.4.0   | 31/12/2023   | Uptake blas-common 4.8.0 and add Helm                   |
| 2.3.0   | 08/12/2023   | Support Micrometer Prometheus metrics                   |
| 2.2.0   | 06/12/2023   | Support Github Packages Maven Repository                |
| 2.1.0   | 01/12/2023   | Upgrade spring boot 3.2.0                               |
| 2.0.0   | 23/09/2023   | Support OAuth2                                          |
| 1.24.0  | 22/09/2023   | Uptake blas-common 4.1.0                                |
| 1.23.0  | 27/08/2023   | Refactor log and fix warning message                    |
| 1.22.0  | 21/08/2023   | Change to user username to verify account               |
| 1.21.0  | 31/07/2023   | Support Telegram message                                |
| 1.20.0  | 12/06/2023   | Support check maintenance time and Spring boot actuator |
| 1.19.0  | 02/06/2023   | Upgrade blas-common 3.0.0                               |
| 1.18.0  | 02/06/2023   | Fix account already active                              |
| 1.17.0  | 25/05/2023   | Upgrade spring boot 3.1.0                               |
| 1.16.0  | 25/04/2023   | Uptake blas-common 2.6.0                                |
| 1.15.0  | 03/04/2023   | Uptake blas-common 2.3.0                                |
| 1.14.0  | 30/03/2023   | Upgrade Spring boot 3.0.5                               |
| 1.13.0  | 23/03/2023   | Uptake blas-common 2.0.0                                |
| 1.12.0  | 19/03/2023   | Support monitoring blas-email                           |
| 1.11.0  | 17/03/2023   | Support email template                                  |
| 1.10.0  | 15/03/2023   | Fix bug                                                 |
| 1.9.0   | 10/11/2022   | Apply Centralized Logger                                |
| 1.8.0   | 23/10/2022   | Add register, verification code feature                 |
| 1.7.0   | 09/10/2022   | Fix Sonarqube issues                                    |
| 1.6.0   | 06/10/2022   | Update version                                          |
| 1.5.0   | 01/09/2022   | Enhance response body                                   |
| 1.4.0   | 01/09/2022   | Enhance register feature                                |
| 1.3.1   | 06/08/2022   | Refactor code                                           |
| 1.2.2   | 08/07/2022   | Add block user and register feature                     |
| 1.1.2   | 16/06/2022   | Refactor code                                           |
| 1.1.1   | 14/06/2022   | Update authentication policy                            |
| 1.0.0   | 12/06/2022   | The first release                                       |
