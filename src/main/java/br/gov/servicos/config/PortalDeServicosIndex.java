package br.gov.servicos.config;

import br.gov.servicos.metricas.Opiniao;
import br.gov.servicos.servico.Servico;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static br.gov.servicos.foundation.IO.read;
import static lombok.AccessLevel.PRIVATE;

@Component
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class PortalDeServicosIndex {

    public static final String IMPORTADOR = "pds-importador";
    public static final String PERSISTENTE = "pds-persistente";

    private static final String SETTINGS = "/elasticsearch/settings.json";

    ElasticsearchTemplate es;

    @Autowired
    public PortalDeServicosIndex(ElasticsearchTemplate es) {
        this.es = es;
    }

    private static String settings() throws IOException {
        ClassPathResource resource = new ClassPathResource(SETTINGS);

        return read(resource);
    }

    @CacheEvict(value={"buscas", "conteudo", "destaques", "orgaos", "linhasDaVida", "publicosAlvo"}, allEntries=true)
    public void recriar() throws IOException {
        recriarIndiceImportador();
        criarIndicePersistenteSeNaoExistir();

        es.putMapping(Servico.class);
        es.putMapping(Opiniao.class);
    }

    private void criarIndicePersistenteSeNaoExistir() throws IOException {
        if (!es.indexExists(PERSISTENTE))
            es.createIndex(PERSISTENTE, settings());
    }

    private void recriarIndiceImportador() throws IOException {
        if (es.indexExists(IMPORTADOR)) {
            es.deleteIndex(IMPORTADOR);
        }
        es.createIndex(IMPORTADOR, settings());
    }

}
