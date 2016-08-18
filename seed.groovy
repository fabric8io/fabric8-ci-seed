import jenkins.model.*

import javax.xml.transform.stream.StreamSource

def organisation = 'fabric8-quickstarts'

repoApi = new URL("https://api.github.com/orgs/${organisation}/repos?per_page=5000")
repos = new groovy.json.JsonSlurper().parse(repoApi.newReader())
repos.each {
  def repoName = it.name

  // lets check if the repo has a pom.xml
  pomUrl = new URL("https://raw.githubusercontent.com/${organisation}/${repoName}/master/pom.xml")
  def hasPom = false
  try {
    hasPom = !pomUrl.text.isEmpty()
  } catch (e) {
    // ignore
  }

// lets check if the repo has a settings.xml
  settingsUrl = new URL("https://raw.githubusercontent.com/${organisation}/${repoName}/master/configuration/settings.xml")
  def hasSettingsXml = false
  try {
    hasSettingsXml = !settingsUrl.text.isEmpty()
  } catch (e) {
    // ignore
  }

  if (hasPom) {
    def pullReqJobName = "${repoName}-pullreq".replaceAll('/', '-')
    def pullReqMergeJobName = "${repoName}-pullreq-merge".replaceAll('/', '-')

    def prXml = pullReqXml(organisation, repoName)
    if (hasSettingsXml) {
      prXml = prXml.replaceAll("</targets>", " -s configuration/settings.xml</targets>")
    }
    createOrUpdateJob(pullReqJobName, prXml)
    def prMergeXml = pullReqMergeXml(organisation, repoName)
    if (hasSettingsXml) {
      prMergeXml = prMergeXml.replaceAll("</targets>", " -s configuration/settings.xml</targets>")
    }
    createOrUpdateJob(pullReqMergeJobName, prMergeXml)
  } else {
    println("ignoring project ${repoName} as we could not find a pom.xml")
  }
}

def createOrUpdateJob(String jobName, String xml) {
  println "Updating job ${jobName}..."
  //println "has XML: " + xml

  def item = Jenkins.instance.getItemByFullName(jobName)
  if (item == null) {
    def xmlStream = new ByteArrayInputStream(xml.getBytes())
    Jenkins.instance.createProjectFromXML(jobName, xmlStream)
  } else {
    xmlSource = new StreamSource(new StringReader(xml))
    item.updateByXml(xmlSource)
    item.save()
  }
  println "Updated job ${jobName}"
}

def pullReqXml(String organisation, String repoName) {
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
          "        <name>\${sha1}</name>\n" +
          "      </hudson.plugins.git.BranchSpec>\n" +
          "    </branches>\n" +
          "    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>\n" +
          "    <submoduleCfg class=\"list\"/>\n" +
          "    <extensions>\n" +
          "      <hudson.plugins.git.extensions.impl.CleanCheckout/>\n" +
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
          "      <adminlist>jimmidyson iocanel rawlingsj davsclaus jstrachan dhirajsb chirino nicolaferraro lburgazzoli jamesnetherton astefanutti</adminlist>\n" +
          "      <allowMembersOfWhitelistedOrgsAsAdmin>false</allowMembersOfWhitelistedOrgsAsAdmin>\n" +
          "      <orgslist></orgslist>\n" +
          "      <cron>H/5 * * * *</cron>\n" +
          "      <buildDescTemplate></buildDescTemplate>\n" +
          "      <onlyTriggerPhrase>false</onlyTriggerPhrase>\n" +
          "      <useGitHubHooks>true</useGitHubHooks>\n" +
          "      <permitAll>false</permitAll>\n" +
          "      <whitelist> jstrachan janstey rawlingsj davsclaus jimmidyson rhuss oscerd dhirajsb chirino nicolaferraro lburgazzoli jamesnetherton astefanutti</whitelist>\n" +
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

def pullReqMergeXml(String organisation, String repoName) {
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
          "        <name>\${sha1}</name>\n" +
          "      </hudson.plugins.git.BranchSpec>\n" +
          "    </branches>\n" +
          "    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>\n" +
          "    <submoduleCfg class=\"list\"/>\n" +
          "    <extensions>\n" +
          "      <hudson.plugins.git.extensions.impl.CleanCheckout/>\n" +
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
          "      <adminlist>jimmidyson iocanel rawlingsj davsclaus jstrachan dhirajsb chirino nicolaferraro lburgazzoli jamesnetherton astefanutti</adminlist>\n" +
          "      <allowMembersOfWhitelistedOrgsAsAdmin>false</allowMembersOfWhitelistedOrgsAsAdmin>\n" +
          "      <orgslist></orgslist>\n" +
          "      <cron>H/5 * * * *</cron>\n" +
          "      <buildDescTemplate></buildDescTemplate>\n" +
          "      <onlyTriggerPhrase>true</onlyTriggerPhrase>\n" +
          "      <useGitHubHooks>true</useGitHubHooks>\n" +
          "      <permitAll>false</permitAll>\n" +
          "      <whitelist> jstrachan janstey rawlingsj davsclaus jimmidyson rhuss oscerd dhirajsb chirino nicolaferraro lburgazzoli jamesnetherton astefanutti</whitelist>\n" +
          "      <autoCloseFailedPullRequests>false</autoCloseFailedPullRequests>\n" +
          "      <displayBuildErrorsOnDownstreamBuilds>false</displayBuildErrorsOnDownstreamBuilds>\n" +
          "      <whiteListTargetBranches>\n" +
          "        <org.jenkinsci.plugins.ghprb.GhprbBranch>\n" +
          "          <branch></branch>\n" +
          "        </org.jenkinsci.plugins.ghprb.GhprbBranch>\n" +
          "      </whiteListTargetBranches>\n" +
          "      <gitHubAuthId>c1ae79fc-34d7-4888-8cf9-98c0b32ee380</gitHubAuthId>\n" +
          "      <triggerPhrase>.*\\Q[merge]\\E.*</triggerPhrase>\n" +
          "      <extensions>\n" +
          "        <org.jenkinsci.plugins.ghprb.extensions.status.GhprbSimpleStatus>\n" +
          "          <commitStatusContext>merge</commitStatusContext>\n" +
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
          "  <publishers>\n" +
          "    <org.jenkinsci.plugins.ghprb.GhprbPullRequestMerge plugin=\"ghprb@1.31.2\">\n" +
          "      <onlyAdminsMerge>true</onlyAdminsMerge>\n" +
          "      <disallowOwnCode>false</disallowOwnCode>\n" +
          "      <mergeComment>PR merged! Thanks!</mergeComment>\n" +
          "      <failOnNonMerge>true</failOnNonMerge>\n" +
          "      <deleteOnMerge>false</deleteOnMerge>\n" +
          "    </org.jenkinsci.plugins.ghprb.GhprbPullRequestMerge>\n" +
          "  </publishers>\n" +
          "  <buildWrappers/>\n" +
          "</project>"
}
