import java.net.{ServerSocket, Socket}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Try, Using}
import scala.collection.mutable

case class Client(socket: Socket, nickname: Option[String], writer: PrintWriter)

object ChatServer {
  private val clients = mutable.ListBuffer[Client]()
  private val clientsLock = new Object
  
  given ExecutionContext = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val port = if (args.nonEmpty) args(0).toInt else 8888
    println(s"Чат сервер запущен на порту $port")
    
    Using(new ServerSocket(port)) { serverSocket =>
      while (true) {
        val clientSocket = serverSocket.accept()
        Future(handleClient(clientSocket))
      }
    }.get
  }

  def handleClient(socket: Socket): Unit = {
    Using(new BufferedReader(new InputStreamReader(socket.getInputStream))) { reader =>
      Using(new PrintWriter(socket.getOutputStream, true)) { writer =>
        val client = Client(socket, None, writer)
        
        writer.println("Добро пожаловать! Пожалуйста, введите свой ник:")
        val nickname = reader.readLine()
        
        if (nickname == null || nickname.trim.isEmpty) {
          writer.println("Пустой ник. До свидания!")
        } else {
          val nick = nickname.trim
          val registeredClient = client.copy(nickname = Some(nick))
          
          clientsLock.synchronized {
            clients += registeredClient
            broadcast(s"$nick присоединился к чату", exclude = Some(registeredClient))
          }
          
          println(s"$nick присоединился")
          
          var line = reader.readLine()
          while (line != null && !socket.isClosed) {
            val msg = line.trim
            if (msg.nonEmpty) {
              broadcast(s"[$nick] $msg", exclude = Some(registeredClient))
            }
            line = reader.readLine()
          }
        }
        
      }.get // writer
    }.get // reader
    
    clientsLock.synchronized {
      clients.find(_.socket == socket).foreach { c =>
        clients -= c
        c.nickname.foreach { nick =>
          broadcast(s"$nick вышел из чата")
          println(s"$nick отключился")
        }
      }
    }
    
    if (!socket.isClosed) socket.close()
  }

  def broadcast(message: String, exclude: Option[Client] = None): Unit = {
    clientsLock.synchronized {
      clients.foreach { client =>
        if (exclude.forall(_ != client)) {
          Try(client.writer.println(message))
        }
      }
    }
  }
}