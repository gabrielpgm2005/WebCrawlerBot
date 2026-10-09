package org.example

import groovyx.net.http.HttpBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import javax.print.Doc

import static groovyx.net.http.HttpBuilder.configure

static void main(String[] args) {

    CrawlerBot bot = new CrawlerBot()
    //Task1
    bot.downloadFileMesAno()

    //Task2
    bot.salvarHistoricoDeVersoes()

    //Task3
    bot.baixarTabelaDeErros()
}