def organization = 'fabric8-quickstarts'

def mavenVersion = 'maven-3.2.5'
def jdkVersion = 'JDK8'


def xml = pullReqXML("swarm-camel")
println "has XML: " + xml

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

def pullReqXml(String repoName) {
  return "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<project>\n" +
          "  <actions/>\n" +
          "  <description></description>\n" +
          "  <keepDependencies>false</keepDependencies>\n" +
          "  <properties>\n" +
          "    <com.coravy.hudson.plugins.github.GithubProjectProperty plugin=\"github@1.17.1\">\n" +
          "      <projectUrl>https://github.com/${organisation}/${repoName}/</projectUrl>\n" +
          "      <displayName></displayName>\n" +
          "    </com.coravy.hudson.plugins.github.GithubProjectProperty>\n" +
          "  </properties>\n" +
          "  <scm class=\"hudson.plugins.git.GitSCM\" plugin=\"git@2.4.2\">\n" +
          "    <configVersion>2</configVersion>\n" +
          "    <userRemoteConfigs>\n" +
          "      <hudson.plugins.git.UserRemoteConfig>\n" +
          "        <name>origin</name>\n" +
          "        <refspec>+refs/pull/*:refs/remotes/origin/pr/*</refspec>\n" +
          "        <url>git://github.com/${organisation}/${repoName}.git</url>\n" +
          "      </hudson.plugins.git.UserRemoteConfig>\n" +
          "    </userRemoteConfigs>\n" +
          "    <branches>\n" +
          "      <hudson.plugins.git.BranchSpec>\n" +
          "        <name>\${ghprbActualCommit}</name>\n" +
          "      </hudson.plugins.git.BranchSpec>\n" +
          "    </branches>\n" +
          "    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>\n" +
          "    <submoduleCfg class=\"list\"/>\n" +
          "    <extensions>\n" +
          "      <hudson.plugins.git.extensions.impl.CleanCheckout/>\n" +
          "      <hudson.plugins.git.extensions.impl.PreBuildMerge>\n" +
          "        <options>\n" +
          "          <mergeRemote>origin</mergeRemote>\n" +
          "          <mergeTarget>\${ghprbTargetBranch}</mergeTarget>\n" +
          "          <mergeStrategy>default</mergeStrategy>\n" +
          "          <fastForwardMode>NO_FF</fastForwardMode>\n" +
          "        </options>\n" +
          "      </hudson.plugins.git.extensions.impl.PreBuildMerge>\n" +
          "    </extensions>\n" +
          "  </scm>\n" +
          "  <canRoam>true</canRoam>\n" +
          "  <disabled>false</disabled>\n" +
          "  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>\n" +
          "  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>\n" +
          "  <jdk>JDK8</jdk>\n" +
          "  <triggers>\n" +
          "    <org.jenkinsci.plugins.ghprb.GhprbTrigger plugin=\"ghprb@1.31.2\">\n" +
          "      <spec>H/5 * * * *</spec>\n" +
          "      <latestVersion>3</latestVersion>\n" +
          "      <configVersion>3</configVersion>\n" +
          "      <adminlist>jimmidyson iocanel rawlingsj davsclaus jstrachan</adminlist>\n" +
          "      <allowMembersOfWhitelistedOrgsAsAdmin>false</allowMembersOfWhitelistedOrgsAsAdmin>\n" +
          "      <orgslist></orgslist>\n" +
          "      <cron>H/5 * * * *</cron>\n" +
          "      <buildDescTemplate></buildDescTemplate>\n" +
          "      <onlyTriggerPhrase>false</onlyTriggerPhrase>\n" +
          "      <useGitHubHooks>true</useGitHubHooks>\n" +
          "      <permitAll>false</permitAll>\n" +
          "      <whitelist> jstrachan janstey rawlingsj davsclaus jimmidyson rhuss oscerd</whitelist>\n" +
          "      <autoCloseFailedPullRequests>false</autoCloseFailedPullRequests>\n" +
          "      <displayBuildErrorsOnDownstreamBuilds>false</displayBuildErrorsOnDownstreamBuilds>\n" +
          "      <whiteListTargetBranches>\n" +
          "        <org.jenkinsci.plugins.ghprb.GhprbBranch>\n" +
          "          <branch></branch>\n" +
          "        </org.jenkinsci.plugins.ghprb.GhprbBranch>\n" +
          "      </whiteListTargetBranches>\n" +
          "      <gitHubAuthId>c1ae79fc-34d7-4888-8cf9-98c0b32ee380</gitHubAuthId>\n" +
          "      <triggerPhrase></triggerPhrase>\n" +
          "      <extensions>\n" +
          "        <org.jenkinsci.plugins.ghprb.extensions.status.GhprbSimpleStatus>\n" +
          "          <commitStatusContext>test</commitStatusContext>\n" +
          "          <triggeredStatus></triggeredStatus>\n" +
          "          <startedStatus></startedStatus>\n" +
          "          <statusUrl></statusUrl>\n" +
          "          <addTestResults>false</addTestResults>\n" +
          "        </org.jenkinsci.plugins.ghprb.extensions.status.GhprbSimpleStatus>\n" +
          "      </extensions>\n" +
          "    </org.jenkinsci.plugins.ghprb.GhprbTrigger>\n" +
          "  </triggers>\n" +
          "  <concurrentBuild>false</concurrentBuild>\n" +
          "  <builders>\n" +
          "    <hudson.tasks.Maven>\n" +
          "      <targets>clean install -U -Ddocker.skip=true</targets>\n" +
          "      <mavenName>maven-3.2.5</mavenName>\n" +
          "      <usePrivateRepository>false</usePrivateRepository>\n" +
          "      <settings class=\"jenkins.mvn.DefaultSettingsProvider\"/>\n" +
          "      <globalSettings class=\"jenkins.mvn.DefaultGlobalSettingsProvider\"/>\n" +
          "    </hudson.tasks.Maven>\n" +
          "  </builders>\n" +
          "  <publishers/>\n" +
          "  <buildWrappers/>\n" +
          "</project>"
}