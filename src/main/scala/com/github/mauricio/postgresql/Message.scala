package com.github.mauricio.postgresql

object Message {
  val AuthenticationOk = 'R'
  val BackendKeyData = 'K'
  val Bind = 'B'
  val BindComplete = '2'
  val CommandComplete = 'C'
  val Close = 'X'
  val CloseStatementOrPortal = 'C'
  val CloseComplete = '3'
  val DataRow = 'D'
  val Describe = 'D'
  val Error = 'E'
  val Execute = 'E'
  val EmptyQuery = 'I'
  val NoData = 'n'
  val Notice = 'N'
  val Notification = 'A'
  val ParameterStatus = 'S'
  val Parse = 'P'
  val ParseComplete = '1'
  val PortalSuspended = 's'
  val Query = 'Q'
  val RowDescription = 'T'
  val ReadyForQuery = 'Z'
  val Startup : Char = 0
  val Sync = 'S'
}

class Message ( val name : Char, val content : Any ) {

  override def hashCode : Int  = {
    "%s-%s".format( this.name, this.content ).hashCode()
  }

  override def equals( other : Any ) : Boolean = {

    other match {
      case o : Message => {
        this.name.equals(o.name) && this.content.equals( o.content )
      }
      case _ => false
    }

  }

}