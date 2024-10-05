mvn clean verify sonar:sonar \
  -Dsonar.projectKey=blas-idp \
  -Dsonar.projectName='blas-idp' \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=sqp_ac31495f4408044ccfe7d5ac65981761beafc001
