package service

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.i18n.MessagesApi
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers._
import securesocial.core.services.UserService

import scala.collection.immutable.ListMap

/**
  * MyEnvironment is part of OAuth2 authentication process and has been borrowed
  * from {@see <a href="http://www.securesocial.ws/">securesocial</a>} project.
  */
@Singleton
class MyEnvironment @Inject()(override val configuration: Configuration, override val messagesApi: MessagesApi) extends RuntimeEnvironment.Default {
  type U = User
  override val userService: UserService[User] = new MyUserService()

  override lazy val providers = ListMap(
    include(new GitHubProvider(routes, cacheService, oauth2ClientFor(GitHubProvider.GitHub)))
  )
}
