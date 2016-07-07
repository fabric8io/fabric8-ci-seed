def organization = 'fabric8-quickstarts'
repoApi = new URL("https://api.github.com/orgs/${organization}/repos")
repos = new groovy.json.JsonSlurper().parse(repoApi.newReader())
repos.each {
  def repoName = it.name
  def jobName = "${repoName}-pullreq".replaceAll('/', '-')

  println "${jobName}"

  // lets only do this for one job to start with!
  if (repoName == "swarm-camel") {
    mavenJob(jobName) {
      //logRotator(-1, 10)
      jdk('JDK8')
      mavenInstallation('3.2.5')

      git {
        remote {
          github(
                  "${organization}/${jobName}",
                  '${ghprbActual  Commit}'
          )
        }
        branch('master')
        //clean(true)
        //createTag(false)
        //relativeTargetDir('src/github.com/fabric8io/origin-schema-generator')
      }
      triggers {
      }
      goals('clean install -U -Ddocker.skip=true')
    }
  }
}


