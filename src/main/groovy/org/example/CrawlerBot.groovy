package org.example

import groovy.json.JsonOutput
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.w3c.dom.html.HTMLElement

import static groovyx.net.http.HttpBuilder.configure

class CrawlerBot {

    private String webPageUri

    CrawlerBot(){
        def home_page = configure {
            request.uri = 'https://www.gov.br/ans/pt-br'
        }.get() as Document
        //pegar o link para a 2a
        String linkPaginaPrestador = home_page.select('a').
                find {it -> it.text() == 'Espaço do Prestador de Serviços de Saúde'}.attr('href')

        println "Link para a pagina assuntos de prestadores: ${linkPaginaPrestador}"

        //Abrir a 2a pagina
        def paginaPrestador = configure {
            request.uri = linkPaginaPrestador
        }.get() as Document
        //pegar o link para a 3a
        this.webPageUri = paginaPrestador.select('a').find{it ->
            it.select('span span').
                    find{inner -> inner.text() == 'TISS - Padrão para Troca de Informação de Saúde Suplementar'}
        }.attr('href')

        println "Link para a pagina padrão de trocas de informação TISS: ${this.webPageUri}"
    }

    void downloadFileMesAno(){
        //Abrir a 1a pagina
        CrawlerBot bot = new CrawlerBot()


        def webPage = configure{
            request.uri = this.webPageUri
        }.get() as Document

        String linkTissMesAno = webPage.select('a').find{it ->
            it.text() == 'Clique aqui para acessar a versão Setembro/2026'
        }.attr('href')

        println "Link para a pagina da versão mês/ano: ${linkTissMesAno}"

        //Pegar o link para baixar o arquivo Componentes de comunicação
        def mesAnoArquivosPage = configure {
            request.uri = linkTissMesAno
        }.get() as Document

        String linkToDownloadFile = mesAnoArquivosPage.select('a').find { it ->
            it.select('span').find {
                inner -> inner.text() == 'Componente de Comunicação.'
            }
        }.attr('href')

        println "Link para baixar o Arquivo: ${linkToDownloadFile}"

        File destino = new File('/home/gabriel/temp/Crawler/src/downloads/Aquivos_padrao_TISS_dados.zip')

        destino.bytes = configure {
            request.uri = linkToDownloadFile
        }.get() as byte[]
    }

    void salvarHistoricoDeVersoes(){
        //Pegar o link para o histórico de versões e abrir ele
        def mainPage = configure {
            request.uri = this.webPageUri
        }.get() as Document

        String pageVersionsHistory = mainPage.select('a').find {it ->
            it.text() == 'Clique aqui para acessar todas as versões dos Componentes'
        }.attr('href')

        println "Link para a pagina do historico de versoes: ${pageVersionsHistory}"

        def versionsHistoryPage = configure {
            request.uri = pageVersionsHistory
        }.get() as Document

        //Pegar a lista de elementos na tabela
        def fieldsDataPerVersion = versionsHistoryPage
                .getElementById('parent-fieldname-text')
                .getElementsByTag('table')
                .select('tbody > tr')
                .collect { tr ->
                    tr.select('td').collect { td -> td.text() }
                }

        fieldsDataPerVersion.removeAll {
            it[0].trim()[-4..-1].toInteger() <= 2015
        }

        ArrayList mapedData = new ArrayList()

        fieldsDataPerVersion.forEach {
            def data = [
                    competencia: it[0],
                    publicacao: it[1],
                    inicioDeVIgencia: it[2]
            ]
            mapedData.add(data)
        }

        def json = JsonOutput.prettyPrint(
                JsonOutput.toJson(mapedData)
        )
        new File("dados.json").text = json
    }

    void baixarTabelaDeErros(){
        def mainPage = configure {
            request.uri = this.webPageUri
        }.get() as Document

        String pageSheetsUri = mainPage.select('a').find {it ->
            it.text() == 'Clique aqui para acessar as planilhas'
        }.attr('href')

        def sheetsPage = configure {
            request.uri = pageSheetsUri
        }.get() as Document

        String linkToDownloadFile = sheetsPage.select('a').find{
            it.text() == 'Clique aqui para baixar a tabela de erros no envio para a ANS (.xlsx)'
        }.attr('href')

        File destino = new File('/home/gabriel/temp/Crawler/src/downloads/Tabela_de_erros.xlsx')

        destino.bytes = configure {
            request.uri = linkToDownloadFile
        }.get() as byte[]

    }
}
