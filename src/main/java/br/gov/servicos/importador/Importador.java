package br.gov.servicos.importador;

import br.gov.servicos.config.PortalDeServicosIndex;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.FileSystemUtils.deleteRecursively;

@Slf4j
@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ManagedResource(
        objectName = "ServicosGovBr:type=Importador",
        description = "Importa dados para o ElasticSearch"
)
public class Importador {

    PortalDeServicosIndex indices;
    ImportadorCartasDeServico cartasDeServico;
    ImportadorV2 v2;
    ImportadorConteudo conteudo;

    @Autowired
    public Importador(PortalDeServicosIndex indices,
                      ImportadorV2 v2,
                      ImportadorConteudo conteudo,
                      ImportadorCartasDeServico cartasDeServico) {

        this.indices = indices;
        this.v2 = v2;
        this.conteudo = conteudo;
        this.cartasDeServico = cartasDeServico;
    }

    @ManagedOperation
    @SneakyThrows
    public Map<String, Object> importar() {
        log.info("Iniciando importação");
        indices.recriar();

        File repositorioCartas = Files.createTempDirectory("portal-de-servicos").toFile();
        repositorioCartas.deleteOnExit();

        Map<String, Object> retorno = new HashMap<>();
        retorno.put("versao-cartas-de-servico", cartasDeServico.importar(repositorioCartas));
        retorno.put("servicos-v2", v2.importar(repositorioCartas));
        retorno.put("conteudos", conteudo.importar());

        if (!deleteRecursively(repositorioCartas))
            log.warn("Não foi possível excluir clone local do repositório de cartas de servico em {}",
                    repositorioCartas.getAbsolutePath());

        log.info("Importação concluída com sucesso");
        return retorno;
    }
}
