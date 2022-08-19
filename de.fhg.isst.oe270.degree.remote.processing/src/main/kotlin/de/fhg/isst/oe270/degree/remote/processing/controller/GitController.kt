/**
 * Copyright 2020-2022 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhg.isst.oe270.degree.remote.processing.controller

import de.fhg.isst.oe270.degree.remote.processing.configuration.GitConfiguration
import de.fhg.isst.oe270.degree.remote.processing.configuration.IoConfiguration
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@Component("gitController")
class GitController {

    private val logger = LoggerFactory.getLogger("GitController")!!

    @Autowired
    lateinit var ioConfiguration: IoConfiguration

    @Autowired
    lateinit var gitConfiguration: GitConfiguration

    private val git : Git by lazy { buildGitClient() }

    @PostConstruct
    fun init() {
        logger.info("Initializing git system.")
        // this is required to determine if we need to clone the repository
        val cloneRequired = !File(ioConfiguration.gitManagementDir).isDirectory

        if (cloneRequired) {
            logger.info("The repository is not yet initialized. Going to clone it now.")
            // git clone
            Git.cloneRepository()
                    .setDirectory(File(ioConfiguration.workDir))
                    .setBranch("master")
                    .setURI(gitConfiguration.repositoryAddress)
                    .setCredentialsProvider(getGitCredentials())
                    .call()

            val commitCount = getCommitCount()
            logger.info("Cloning finished. There are $commitCount commits in the repository.")

            // check if we need to perform an initial commit
            if (commitCount == 0) {
                logger.info("Since the repository is not yet initialized an empty initial commit will be performed now.")
                // git commit --allow-empty -m "Initial commit."
                git.commit()
                        .setAllowEmpty(true)
                        .setAuthor(getGitIdentity())
                        .setCommitter(getGitIdentity())
                        .setMessage("Initial commit.")
                        .call()
                // git push
                git.push()
                        .setCredentialsProvider(getGitCredentials())
                        .call()
                logger.info("Initial commit performed.")
            }

        } else {
            logger.info("The repository is already initialized. Going to retrieve the most recent commit now.")
            // git remote add origin
            git.remoteSetUrl()
                    .setRemoteName("origin")
                    .setRemoteUri(URIish(gitConfiguration.repositoryAddress))
                    .call()
            // git pull
            git.pull()
                    .setRemote("origin")
                    .setRemoteBranchName("master")
                    .setCredentialsProvider(getGitCredentials())
                    .call()
            logger.info("Update finished.")
        }
        logger.info("Initialization of git system finished.")
    }

    private fun buildGitClient(): Git {
        // initialize the repository
        val repositoryBuilder = RepositoryBuilder()
                .setGitDir(File(ioConfiguration.gitManagementDir))
        return Git(repositoryBuilder.build())
    }

    /**
     * Calculate the number of commits for the remote processing repository
     */
    private fun getCommitCount() : Int {
        var commitCount = 0
        val revWalk = RevWalk(git.repository)
        git.repository.allRefsByPeeledObjectId.forEach{ (id, _) ->
            revWalk.markStart(revWalk.parseCommit(id))
            repeat(revWalk.count()) {
                commitCount++
            }
        }

        return commitCount
    }

    fun commitPushGit(message : String, update: Boolean = false) {
        // git add .
        git.add()
                .addFilepattern(".")
                .setUpdate(update)
                .call()
        // git status
        val status = git.status()
                .call()
        if (status.hasUncommittedChanges()) {
            git.commit()
                    .setAuthor(getGitIdentity())
                    .setCommitter(getGitIdentity())
                    .setMessage(message)
                    .call()
            // git push
            git.push()
                    .setCredentialsProvider(getGitCredentials())
                    .call()
            logger.info("Repository changes pushed to origin/master.")
        }
    }

    private fun getGitIdentity() : PersonIdent {
        return PersonIdent(gitConfiguration.identityName, gitConfiguration.identityEmail)
    }

    private fun getGitCredentials() : CredentialsProvider {
        // TODO would be null a valid return type in case no credentials are required?
        return UsernamePasswordCredentialsProvider(gitConfiguration.username, gitConfiguration.password)
    }

    fun cloneRepository(location: String, uri: String, username: String, password: String) {
        Git.cloneRepository()
                .setDirectory(File(location))
                .setBranch("master")
                .setURI(uri)
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(username, password))
                .call().close()
    }

    fun updateRepository(location: String, username: String, password: String) {
        val currentGit = retrieveGitClient(location)
        currentGit.pull()
                .setRemote("origin")
                .setRemoteBranchName("master")
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(username, password))
                .call()
    }

    private fun retrieveGitClient(location: String): Git {
        return Git(RepositoryBuilder()
                        .setGitDir(File(location))
                        .build()
        )
    }

}