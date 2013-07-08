package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object Artisan extends Controller with Authentication {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    ) verifying ("ユーザー名かパスワードが間違っています。", result => result match {
      case (username, password) => Artisans.authenticate(username, password).isDefined
    })
  )

  def login = Action { implicit request =>
    Ok(views.html.artisan.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.login(formWithErrors)),
      { artisan =>
        Artisans.findByUsername(artisan._1).map { artisan_ =>
          Redirect(routes.Artisan.home).withSession("userid" -> artisan_.id.toString)
        }.getOrElse(Forbidden)
      }
    )
  }

  def logout = Action {
    Redirect(routes.Artisan.login).withNewSession.flashing(
      "success" -> "ログアウトしました。"
    )
  }

  def home = IsAuthenticated { userid => _ =>
    Artisans.findById(userid).map { artisan =>
      Ok(views.html.artisan.home(artisan))
    }.getOrElse(Forbidden)
  }

  def articles = IsAuthenticated { userid => _ =>
    Artisans.findById(userid).map { artisan =>
      val articles = Articles.findByAuthorId(userid)
      Ok(views.html.artisan.articles(artisan, articles))
    }.getOrElse(Forbidden)
  }

  val articleForm = Form(
    tuple(
      "title" -> text,
      "text" -> text
    ) verifying ("タイトルまたは本文が空です。", result => result match {
      case ("", _) => false
      case (_, "") => false
      case (_, _) => true
    })
  )

  def createArticle = IsAuthenticated { _ => _ =>
    Ok(views.html.artisan.createArticle(articleForm))
  }

  def postCreateArticle = IsAuthenticated { userid => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createArticle(formWithErrors)),
      { article =>
        Articles.create(userid, article._1, article._2)
        Redirect(routes.Artisan.articles)
      }
    )
  }
}
