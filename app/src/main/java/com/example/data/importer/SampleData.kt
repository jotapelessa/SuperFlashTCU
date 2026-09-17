package com.example.data.importer

import com.example.data.model.FlashcardEntity
import java.security.MessageDigest

object SampleData {

    val sampleCsv = """
Deck;Note type;Field 1;Field 2;Tags
"Auditor TCU/TCEs/TCDF - PARTE 1::Direito Constitucional::a. Constituição: conceito, objeto, elementos e classificações";"Básico+";"Segundo Ferdinand Lassalle, o que é a Constituição no seu sentido sociológico?";"<div style=""text-align: justify;"">Para Ferdinand Lassalle (1825–1864), a Constituição é a <b>soma dos fatores reais do poder</b> que regem uma determinada nação.<br><br><b style=""color: rgb(255, 0, 0);"">RESPOSTA: CONCEITO SOCIOLÓGICO</b><br><br>Se a Constituição escrita (folha de papel) não corresponder aos fatores reais de poder, ela será mera <i>folha de papel</i> (blattes papier).</div>";"constitucional lassalle sentido_sociologico"
"Auditor TCU/TCEs/TCDF - PARTE 1::Direito Constitucional::a. Constituição: conceito, objeto, elementos e classificações";"Básico+";"Qual a concepção de Constituição formulada por Carl Schmitt (sentido político)?";"<div style=""text-align: justify;"">Carl Schmitt distingue <b>Constituição</b> de <b>leis constitucionais</b>:<br><br><b style=""color: rgb(37, 99, 235);"">DECISÃO POLÍTICA FUNDAMENTAL</b><br><br>Constituição corresponde unicamente à decisão política fundamental sobre a forma e o modo da unidade política do Estado. As demais normas são apenas leis constitucionais.</div>";"constitucional schmitt sentido_politico"
"Auditor TCU/TCEs/TCDF - PARTE 1::Direito Constitucional::c. Aplicabilidade das normas constitucionais";"Básico+";"Conforme classificação de José Afonso da Silva, quais as características das normas constitucionais de eficácia contida?";"<div style=""text-align: justify;"">As normas de <b>eficácia contida (ou prospectiva)</b>:<br>• Têm aplicabilidade <b>direta, imediata e não integral</b>;<br>• Já produzem todos os seus efeitos desde a promulgação da CF/88;<br>• Podem sofrer restrição ou contenção por lei infraconstitucional superveniente.<br><br><b style=""color: rgb(16, 185, 129);"">REGRA: NASCEM PLENAS, MAS PODEM SER RESTRINGIDAS</b></div>";"constitucional eficacia_contida jose_afonso"
"Auditor TCU/TCEs/TCDF - PARTE 1::Direito Constitucional::c. Aplicabilidade das normas constitucionais";"Básico+";"Qual a diferença fundamental entre normas de eficácia limitada de princípio institutivo e de princípio programático?";"<div style=""text-align: justify;"">Ambas têm aplicabilidade <b>mediata e diferida</b>, contudo:<br><br><b>1. Princípio institutivo (ou organizativo):</b> traçam esquemas gerais para estruturação ou atribuição de órgãos/entidades.<br><b>2. Princípio programático:</b> definem metas, fins sociais e econômicos a serem perseguidos pelo Estado (ex: erradicação da pobreza).</div>";"constitucional eficacia_limitada programatica"
"Auditor TCU/TCEs/TCDF - PARTE 1::Direito Administrativo::Atos Administrativos: requisitos, atributos e classificação";"Básico+";"Quais são os 5 requisitos de validade dos atos administrativos?";"<div style=""text-align: justify;"">O mnemônico clássico <b>COM-FI-FO-MO-OB</b>:<br><br>1. <b>Competência</b> (sempre vinculado);<br>2. <b>Finalidade</b> (interesse público, vinculado);<br>3. <b>Forma</b> (regra: escrita, vinculado);<br>4. <b>Motivo</b> (pressupostos de fato e direito);<br>5. <b>Objeto</b> (conteúdo do ato).<br><br><b style=""color: rgb(217, 119, 6);"">ATENÇÃO: Apenas Motivo e Objeto comportam discricionariedade!</b></div>";"administrativo atos_administrativos requisitos"
"Auditor TCU/TCEs/TCDF - PARTE 1::Controle Externo::Competências Constitucionais do TCU";"Básico+";"O Tribunal de Contas da União pode anular diretamente contratos administrativos?";"<div style=""text-align: justify;""><b style=""color: rgb(255, 0, 0);"">NÃO! CUIDADO!</b><br><br>Segundo o art. 71, § 1º e § 2º da CF/88:<br>• No caso de <b>ato administrativo</b>: o TCU fixa prazo para que o órgão adote as providências; não atendido, o próprio TCU susta a execução do ato.<br>• No caso de <b>contrato administrativo</b>: o ato de sustação é de competência direta do <b>Congresso Nacional</b>. Apenas se o Congresso não deliberar em 90 dias, o TCU pode decidir a respeito.</div>";"controle_externo tcu constituicao_art71"
"Técnico Judiciário TRF::Direito Processual Civil::Prazos Processuais e Citações";"Básico+";"Como são contados os prazos processuais no CPC/2015?";"<div style=""text-align: justify;"">Conforme o art. 219 do CPC/2015:<br>Na contagem de prazos em dias, estabelecido por lei ou pelo juiz, computar-se-ão <b>somente os dias úteis</b>.<br><br><b style=""color: rgb(13, 148, 136);"">REGRA GERAL DO CPC: DIAS ÚTEIS (art. 219)</b></div>";"processo_civil prazos cpc2015"
"Técnico Judiciário TRF::Direito Processual Civil::Prazos Processuais e Citações";"Básico+";"Qual o prazo para interposição do recurso de embargos de declaração no CPC/2015?";"<div style=""text-align: justify;"">O prazo para interposição de <b>Embargos de Declaração</b> é de <b>5 (cinco) dias úteis</b> (art. 1.023 do CPC).<br><br><i>Nota:</i> É o único recurso cível com prazo de 5 dias; os demais recursos têm prazo geral de 15 dias.</div>";"processo_civil embargos_declaracao recursos"
"Técnico Judiciário TRF::Direito Constitucional::Direitos e Garantias Fundamentais";"Básico+";"Cabe mandado de segurança coletivo impetrado por partido político sem representação no Congresso Nacional?";"<div style=""text-align: justify;""><b style=""color: rgb(255, 0, 0);"">NÃO!</b><br><br>O art. 5º, LXX, 'a' da CF/88 exige expressamente que o partido político possua <b>representação no Congresso Nacional</b> (ao menos um deputado federal ou um senador em exercício).</div>";"processo_constitucional mandado_de_seguranca"
    """.trimIndent()

    fun getInitialCards(): List<FlashcardEntity> {
        val result = AnkiCsvImporter.parseAnkiCsv(sampleCsv)
        // Mark a couple cards with sample review history so Domain and Review tabs show immediate rich realistic stats
        val now = System.currentTimeMillis()
        val oneDay = 24L * 3600L * 1000L
        return result.validCards.mapIndexed { index, card ->
            when (index) {
                0 -> card.copy(
                    intervalDays = 14,
                    easeFactor = 2.6f,
                    reps = 4,
                    lapses = 0,
                    masteryLevel = 2, // Dominado
                    dueTimestamp = now + (10 * oneDay),
                    lastReviewedTimestamp = now - (4 * oneDay)
                )
                1 -> card.copy(
                    intervalDays = 3,
                    easeFactor = 2.4f,
                    reps = 2,
                    lapses = 1,
                    masteryLevel = 1, // Em aprendizado
                    dueTimestamp = now - (1 * oneDay), // Due now!
                    lastReviewedTimestamp = now - (4 * oneDay)
                )
                2 -> card.copy(
                    intervalDays = 21,
                    easeFactor = 2.7f,
                    reps = 5,
                    lapses = 0,
                    masteryLevel = 2, // Dominado
                    dueTimestamp = now + (15 * oneDay),
                    lastReviewedTimestamp = now - (6 * oneDay)
                )
                4 -> card.copy(
                    intervalDays = 1,
                    easeFactor = 2.2f,
                    reps = 1,
                    lapses = 0,
                    masteryLevel = 1, // Em aprendizado
                    dueTimestamp = now - (2 * oneDay), // Due now!
                    lastReviewedTimestamp = now - (3 * oneDay)
                )
                else -> card // New cards
            }
        }
    }
}
