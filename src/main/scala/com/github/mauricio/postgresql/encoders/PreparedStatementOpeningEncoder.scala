package com.github.mauricio.postgresql.encoders

import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import com.github.mauricio.postgresql.util.Log
import com.github.mauricio.postgresql.messages.{PreparedStatementOpeningMessage}
import com.github.mauricio.postgresql.{Message, ChannelUtils, FrontendMessage, CharsetHelper}
import com.github.mauricio.postgresql.column.ColumnEncoderDecoder


/**
 * User: Maurício Linhares
 * Date: 3/7/12
 * Time: 9:20 AM
 */

object PreparedStatementOpeningEncoder extends Encoder {

  private val log = Log.getByName("PreparedStatementOpeningEncoder")

  override def encode(message: FrontendMessage): ChannelBuffer = {

    val m = message.asInstanceOf[PreparedStatementOpeningMessage]

    val queryBytes = CharsetHelper.toBytes(m.query)
    val columnCount = m.valueTypes.size

    val parseBuffer = ChannelBuffers.dynamicBuffer( 1024 )

    parseBuffer.writeByte(Message.Parse)
    parseBuffer.writeInt(0)

    parseBuffer.writeBytes(queryBytes)
    parseBuffer.writeByte(0)
    parseBuffer.writeBytes(queryBytes)
    parseBuffer.writeByte(0)

    parseBuffer.writeShort(columnCount)

    for ( kind <- m.valueTypes ) {
        parseBuffer.writeInt(kind)
    }

    ChannelUtils.writeLength(parseBuffer)

    val bindBuffer = ChannelBuffers.dynamicBuffer(1024)

    bindBuffer.writeByte(Message.Bind)
    bindBuffer.writeInt(0)

    bindBuffer.writeBytes(queryBytes)
    bindBuffer.writeByte(0)
    bindBuffer.writeBytes(queryBytes)
    bindBuffer.writeByte(0)

    bindBuffer.writeShort(0)

    bindBuffer.writeShort(m.values.length)

    for ( value <- m.values ) {
      if ( value == null ) {
        bindBuffer.writeInt(-1)
      } else {
        val encoded = ColumnEncoderDecoder.encode(value).getBytes( CharsetHelper.Unicode )
        bindBuffer.writeInt(encoded.length)
        bindBuffer.writeBytes( encoded )
      }
    }

    bindBuffer.writeShort(0)

    ChannelUtils.writeLength(bindBuffer)

    val describeLength = 1 + 4 + 1 + queryBytes.length + 1
    val describeBuffer = ChannelBuffers.buffer( describeLength )
    describeBuffer.writeByte(Message.Describe)
    describeBuffer.writeInt(describeLength - 1)

    describeBuffer.writeByte('P')

    describeBuffer.writeBytes(queryBytes)
    describeBuffer.writeByte(0)

    val executeLength = 1 + 4 + queryBytes.length + 1 + 4
    val executeBuffer = ChannelBuffers.buffer( executeLength )
    executeBuffer.writeByte(Message.Execute)
    executeBuffer.writeInt(executeLength - 1)

    executeBuffer.writeBytes(queryBytes)
    executeBuffer.writeByte(0)

    executeBuffer.writeInt(0)

    val closeLength = 1 + 4 + 1 + queryBytes.length + 1
    val closeBuffer = ChannelBuffers.buffer(closeLength)
    closeBuffer.writeByte(Message.CloseStatementOrPortal)
    closeBuffer.writeInt( closeLength - 1 )
    closeBuffer.writeByte('P')

    closeBuffer.writeBytes(queryBytes)
    closeBuffer.writeByte(0)

    val syncBuffer = ChannelBuffers.buffer(5)
    syncBuffer.writeByte(Message.Sync)
    syncBuffer.writeInt(4)

    ChannelBuffers.wrappedBuffer(parseBuffer, bindBuffer, describeBuffer, executeBuffer, closeBuffer, syncBuffer)

  }

}
