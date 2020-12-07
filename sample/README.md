
### How to run Gradle Profiler on sample app

curl --request POST \
 --url https://${CIRCLE_CI_TOKEN}:@circleci.com/api/v2/project/gh/dipien/bye-bye-jetifier/pipeline \
 --header 'content-type: application/json' \
 --data '{"branch":"master","parameters":{"api":true, "gradleProfiler":true}}'