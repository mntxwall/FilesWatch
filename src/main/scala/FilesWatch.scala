import java.io.{BufferedInputStream, BufferedOutputStream, FileInputStream, FileOutputStream}
import java.nio.file._
import java.util.zip.{ZipEntry, ZipInputStream}

import com.typesafe.config.{Config, ConfigFactory}

object FilesWatch {
  def main(args: Array[String]): Unit = {

    val config: Config = ConfigFactory.load()

    println(config.getString("wei.test"))
    watch(config)

  }

  /*
  *
  * watch 用来临视文件夹
  * @config 用来读取配置文件
  * */
  def watch(config:Config):Unit = {

    val watchService:WatchService
    = FileSystems.getDefault.newWatchService

    //val path = Paths.get("/home/cw/Work")
    //val unzipPath = Paths.get("/home/cw/unzipFiles")
    val path = Paths.get(config.getString("wei.source"))
    val unzipPath = Paths.get(config.getString("wei.unzipDir"))
    path.register(
      watchService,
      StandardWatchEventKinds.ENTRY_CREATE)

    var key:WatchKey = watchService.take()

    while (key != null){
      key.pollEvents().forEach{ x =>

        println(s"Event kind: ${x.kind} . File affected: ${x.context}")

        Thread.sleep(config.getInt("wei.wait"))
        unzipFiles(unzipPath, path.resolve(x.context.toString), config)
        println("unzip End")

      }
      key.reset()
      key = watchService.take()
    }
  }

  def isZipFile(source: Path, config: Config):Boolean = {

    val is = new FileInputStream(source.toFile)
    val b = new Array[Byte](5)
    is.read(b, 0, b.length)

    if(convertBytesToHex(b.toSeq) ==  config.getString("wei.type")) true
    else false

  }

  def convertBytesToHex(bytes: Seq[Byte]): String = {
    val sb = new StringBuilder
    for (b <- bytes) {
      sb.append(String.format("%02x", Byte.box(b)))
    }
    sb.toString
  }

  def unzipFiles(outDir: Path, source: Path, config: Config):Unit = {

    val buffer = new Array[Byte](1024)
    val stream = new ZipInputStream(new BufferedInputStream(new FileInputStream(source.toFile)))
    val entry = stream.getNextEntry
    /*
    *在做解压之前，需做好文件目录设置
    * 判断文件是否是压缩文件
    * */
    if (isZipFile(source, config)){

      /*
      * 把文件名分出来
      * 根据文件名来确定最终的解压目录
      * */
      //文件名
      val fileNamesToDirSeq:Seq[String] = source.getFileName.toString.split("\\.").toSeq.apply(0).split("_").toSeq
      /*
      *使用foldLeft把对应的目录建好
      * */
      val resultDir: String = fileNamesToDirSeq.foldLeft(outDir.toString){(path1: String, path2: String) => {
        val t = s"$path1/$path2"
        //println(outDir.resolve(t).toString)
        val filePath = outDir.resolve(t)
        if(!Files.isDirectory(filePath)) Files.createDirectories(filePath)
        t
      }}

      doReadZipFile(entry, Paths.get(resultDir), buffer, stream)

    }
    else println("Not Zip files")

    //else  println("Error")
    //
    stream.close()
  }

  def doReadZipFile(entry: ZipEntry, outDir: Path, buffer: Array[Byte], stream: ZipInputStream):Int = {
    entry match {
      case s:ZipEntry => {
        println("Unzipping: " + s.getName)
        val filePath = outDir.resolve(s.getName)
        val bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile), buffer.length)
        val size: Int = stream.read(buffer)
        doWriteUnzipFiles(size, bos, buffer, stream)
        bos.flush()
        doReadZipFile(stream.getNextEntry, outDir, buffer, stream)
        /*
        *  close every stream when call ends
        * */
        bos.close()
        1
      }
      case _ => 0
    }
  }
  def doWriteUnzipFiles(size: Int, bos: BufferedOutputStream, buffer: Array[Byte], stream: ZipInputStream):Int = {

    size match {
      case x if x != -1 => {
        bos.write(buffer, 0, x)
        doWriteUnzipFiles(stream.read(buffer), bos, buffer, stream)
      }
      case _ => 0
    }

  }


}
