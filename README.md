grails-request-performance
==========================

Resumo
------
Projeto contendo o teste de uma solução para melhorar a perfomance de um projeto grails,
a motivação e o contexto do desenvolvimento podem ser vistos no meu blog [Post](jhenrique.me/test).
Nessa solução as requisições para arquivos js são reduzidas com a criação automática de "arquivões" contendo
um conjunto de arquivos, dessa forma ao invés de carregamos 5 arquivo javascript, por exemplo, carregamos
apenas um.
O projeto Grails contido na pasta "grails-project" é bem simples, contendo basicamente os arquivos necessários na implementação
da solução e alguns arquivos javascript para ver tudo funcionando.

Arquivos necessários
--------------------

## _Events.groovy
Esse arquivo pode ser encontrado na pasta "scripts" dentro do projeto grails, é nele que configuramos hooks
para os eventos lançados no build do projeto.

## bundle.json
É o arquivo de configuração, responsável por indicar quais os grupos de arquivo js, onde cada grupo dará
origem a um "arquivão" contendo vários arquivos js compactados.
O seguinte código mostra um exemplo da flexibilidade da configuração:

    {
      "bundles": {
          "js": [
              {
                "output": "all.js",
                "compress": true,
                "dirs": [
                      {
                        "path": "folder1"
                      },
                      {	
                        "path": "folder1/folder2"
                      },
                      {
                        "path": "folder1/folder3",
                        "files": ["file5", "file4"]
                      }
                  ]
              }
          ]	
       }
    }
    
### Atributos

#### Definição de grupo
A definição de um grupo é representada por cada objeto contido no array dentro do atributo "js", no exemplo
anterior temos apenas um grupo, que terá como saída o arquivo "all.js".

#### Atributos do grupo
Cada grupo pode ter o seguinte conjunto de atributos:

+ [String] output: Caminho mais nome do arquivo que será gerado dentro da pasta "webapp/js";
+ [Boolean] compress: Se for true comprime o arquivão utilizando a bilioteca YUICompressor;
+ [Array] dirs: Array contendo os diretórios que alimentarão o arquivo descrito em "output".

#### Atributos para cada diretório
Cada diretório deve obrigatoriamente conter o "path" e opcionalmente o atributo "files":

+ [String] path: Contém o caminho da pasta, tendo como raiz a pasta "webapp/js";
+ [Array] files: Um array que contém o nome dos arquivos dentro do "path" que devem ser lidos. Com esse atributo você pode tanto definir a ordem de carregamento (A ordem definida nesse array, é a ordem de adição do arquivo no grupo), como fazer um carregamento seletivo de arquivos. Caso esse atributo tenha um valor nulo ou vazio, todos os arquivos do "path" serão lidos.
