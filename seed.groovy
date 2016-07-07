def organization = 'fabric8-quickstarts'

def mavenVersion = 'maven-3.2.5'
def jdkVersion = 'JDK8'

repoApi = new URL("https://api.github.com/orgs/${organization}/repos")
repos = new groovy.json.JsonSlurper().parse(repoApi.newReader())
repos.each {
  def repoName = it.name
  def jobName = "${repoName}-pullreq".replaceAll('/', '-')

  println "${jobName}"

  // lets only do this for one job to start with!
  if (repoName == "swarm-camel") {
    job(jobName) {
      //logRotator(-1, 10)

      scm {
        git {
          remote {
            github(
                    "${organization}/${repoName}",
                    'git'
            )
          }
          branch('${ghprbActualCommit}')
          //clean(true)
          //createTag(false)
          //relativeTargetDir('src/github.com/fabric8io/origin-schema-generator')
        }
      }
      triggers {
      }

      steps {
          maven {
            jdk(jdkVersion)
            mavenInstallation(mavenVersion)

            goals('clean install -U -Ddocker.skip=true')
          }
      }

    }
  }
}


