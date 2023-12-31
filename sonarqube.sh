mvn clean verify sonar:sonar \
  -Dsonar.projectKey=blas-idp \
  -Dsonar.projectName='blas-idp' \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=sqp_2abcf4f133d5500e777921ec96e65d8f82adf58b
