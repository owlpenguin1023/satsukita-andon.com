package models

import play.api.db._
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Artisan(id: Int, name: String, username: String, password: String, times: OrdInt)

object Artisan extends Table[Artisan]("ARTISAN") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def id = column[Int]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.NotNull)
  def username = column[String]("USERNAME", O.NotNull, O.PrimaryKey)
  def password = column[String]("PASSWORD", O.NotNull)
  def times = column[Int]("TIMES", O.NotNull)

  def * = id ~ name ~ username ~ password ~ times <> (
    (id, name, username, password, times) => Artisan(id, name, username, password, OrdInt(times)),
    artisan => Some((artisan.id, artisan.name, artisan.username, artisan.password, artisan.times.n))
  )

  val query = Query(Artisan)

  def findById(id: Int): Option[Artisan] = db.withSession { implicit session: Session =>
    query.filter(_.id === id).firstOption
  }

  def findByUsername(username: String): Option[Artisan] = db.withSession { implicit session: Session =>
    query.filter(_.username === username).firstOption
  }

  def authenticate(username: String, password: String) = db.withSession { implicit session: Session =>
    query.filter(_.username === username).filter(_.password === password).firstOption
  }
}
