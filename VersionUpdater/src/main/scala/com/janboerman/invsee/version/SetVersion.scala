package com.janboerman.invsee.version

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{FileVisitResult, Files, OpenOption, StandardOpenOption, Path, Paths, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import scala.xml.{Node, Text, XML}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.io.Source

@main def main(args: String): Unit = {

    val rootVersion: String = readVersion(new File("pom.xml"));
    val newVersion: String = if args.nonEmpty then args else null

    val start: Path = Paths.get(".");
    Files.walkFileTree(start, new PomFileVisitor(rootVersion, newVersion))

}

private def readVersion(pomFile: File): String = {
    val parser = scala.xml.parsing.ConstructingParser.fromSource(Source.fromFile(pomFile), false);
    val project = parser.document()
    val version = project \ "version"
    version.text
}

class UpdateVersionRule(rootVersion: String, newVersion: String) extends RewriteRule {
    override def transform(node: Node): Seq[Node] = {
        if node.text == rootVersion && newVersion != null then
            Text(newVersion)
        else if newVersion == null then
            Text(rootVersion)
        else
            node
    }
}

class PomFileVisitor(val rootVersion: String, val newVersion: String) extends SimpleFileVisitor[Path] {

    private val transformer = new RuleTransformer(new UpdateVersionRule(rootVersion, newVersion))

    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (file.toString.endsWith("pom.xml")) {
            val xmlSource = scala.xml.XML.loadFile(file.toFile);
            val newXml = transformer.transform(xmlSource).head
            Files.writeString(file, newXml.toString, StandardCharsets.UTF_8, Array[OpenOption](StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING): _*)
        }

        FileVisitResult.CONTINUE
    }

}