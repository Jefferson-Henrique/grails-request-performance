import grails.converters.JSON;

import org.apache.tools.ant.filters.StringInputStream
import org.codehaus.groovy.grails.web.json.JSONElement;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor

/**
 * Classe responsável por ler um arquivo de configurações JSON
 * e criar grupos de arquivos para cada combinação configurada 
 * 
 * @author Jefferson Henrique
 */
class BundleScripts {
	
	// Charset padrão
	private static final String CHARSET = "utf-8"
	
	// Filtra somente por arquivos javascript
	private static final FilenameFilter FILTER_JS = new FilenameFilter(){
		boolean accept(File f, String a) {
			return a.endsWith(".js")
		}
	}

	// String que será colocada entre os arquivos
	private static final String GAP_CONTENT = "\n"
	
	/**
	 * Método responsável por devolver o caminho relativo de um arquivo
	 * js
	 * 
	 * @param name Local do arquivo
	 * @return Caminho relativo do arquivo js dentro do projeto Grails
	 */
	private static String getPathFile(name) {
		return "web-app/js/${name}"
	}
	
	/**
	 * Adiciona o conteúdo de arquivo ao buffer informado como parâmetro.
	 * Caso o arquivo a ser lido seja o mesmo o caminho do arquivo final, a
	 * adição no buffer será ignora
	 * 
	 * @param buffer Buffer para o qual será adicionado o conteúdo do arquivo
	 * @param outputFile Arquivo final que será gerado
	 * @param f Arquivo que terá seu conteúdo lido
	 */
	private static void appendBuffer(StringBuilder buffer, File outputFile, File f) {
		// Se o arquivo for o mesmo do arquivo final, o resto do método será ignorado
		if (outputFile.absolutePath == f.absolutePath) {
			return;
		}
		
		// Essa parte é importante, pois o arquivo JS é encapsulado dentro de um bloco
		// de código para evitar que haja conflito entre os arquivos compactados
		buffer.append("(function(){")
		buffer.append(f.text)
		buffer.append("}).call(this);")
		buffer.append(GAP_CONTENT)
	}
	
	/**
	 * Método principal que juntará e minificará os arquivos
	 */
	public static void bundle() {
		println "Creating bundles..."
		
		// Utilizando biblioteca de JSON do Grails para realizar a leitura do arquivo
		// de configuração
		JSONElement bundleJson = JSON.parse(new File("bundle.json").text);
		JSONElement bundles = bundleJson.bundles;
		
		bundles.each {
			def obj = it
			for (exec in obj.value) {
				def output = exec.output
				
				// Arquivo que será gerado ao final da compactação
				File outputFile = new File(getPathFile(output))

				def buffer = new StringBuilder()
								
				for (dir in exec.dirs) {
					def fileDir = new File(getPathFile(dir.path))

					// Se não forem inforamados arquivos, todos os arquivos JS
					// do diretório em questão serão adicionados
					if (dir.files == null || dir.files.size == 0) {
						def files = fileDir.listFiles(FILTER_JS)
						for (f in files) {
							appendBuffer(buffer, outputFile, f);
						}
					} else {
						// Cada arquivo infomado na configuração é carregado na
						// ordem definida na configuração
						for (f in dir.files) {
							appendBuffer(buffer, outputFile, new File(fileDir, "${f}.js"));
						}
					}
				}
				
				String finalContent = buffer.toString()
				
				// Executada compressão caso seja informado na configuração
				if (exec.compress) {
					def isr = new InputStreamReader(new StringInputStream(finalContent, CHARSET))
					def jvc = new JavaScriptCompressor(isr, null)
					isr.close()
					
					def writer = new OutputStreamWriter(new FileOutputStream(outputFile), CHARSET)
					jvc.compress(writer, 1, false, false, false, false)
					writer.close()
				} else {
					// Caso não tenha sido configurado para ser comprimido
					// o arquivo final é formado apenas pela concatenação dos arquivos informados
					outputFile.text = finalContent
				}
			}
		}
		
		println "Bundle created"
	}
	
}

// Executado ao fim da fase de compilação
eventCompileEnd = {
	BundleScripts.bundle()
}