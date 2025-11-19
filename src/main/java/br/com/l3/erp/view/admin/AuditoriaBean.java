package br.com.l3.erp.view.admin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import br.com.l3.erp.model.dao.auditoria.AuditoriaLogDAO;
import br.com.l3.erp.model.entity.auditoria.AuditoriaLog;
import br.com.l3.erp.model.entity.auditoria.TipoAcao;

@Named
@ViewScoped
public class AuditoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AuditoriaLogDAO auditoriaLogDAO;

    private List<AuditoriaLog> logs;
    private AuditoriaLog logSelecionado;

    // Campos para o filtro de data
    private LocalDate dataInicio;
    private LocalDate dataFim;
    
    // Arquivo para download
    private StreamedContent arquivoPDF;

    @PostConstruct
    public void init() {
        this.logs = auditoriaLogDAO.listarTodos();
        // Define datas padrão (ex: últimos 30 dias)
        this.dataInicio = LocalDate.now().minusDays(30);
        this.dataFim = LocalDate.now();
    }

    public void gerarRelatorioPDF() {
        if (dataInicio == null || dataFim == null) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Selecione as datas de início e fim."));
            return;
        }
        
        if (dataInicio.isAfter(dataFim)) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "A data inicial não pode ser maior que a final."));
            return;
        }

        try {
            // Converte LocalDate para LocalDateTime (início do dia 00:00 e fim do dia 23:59)
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

            List<AuditoriaLog> logsFiltrados = auditoriaLogDAO.buscarPorIntervalo(inicio, fim);

            if (logsFiltrados.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Nenhum registro encontrado no período."));
                return;
            }

            // --- GERAÇÃO DO PDF ---
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate()); // Paisagem para caber mais colunas
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Relatório de Auditoria", fontTitulo);
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(titulo);
            
            document.add(new Paragraph("Período: " + dataInicio + " a " + dataFim));
            document.add(new Paragraph("Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph(" ")); // Espaço

            // Tabela
            PdfPTable table = new PdfPTable(5); // 5 Colunas
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 2f, 2f, 4f}); // Larguras relativas

            // Cabeçalho da Tabela
            String[] colunas = {"Data/Hora", "Usuário", "Ação", "Entidade", "Detalhes (Resumo)"};
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            
            for (String col : colunas) {
                PdfPCell cell = new PdfPCell(new Phrase(col, fontHeader));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Dados
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10);

            for (AuditoriaLog log : logsFiltrados) {
                table.addCell(new Phrase(log.getTimestamp().format(dtf), fontDados));
                table.addCell(new Phrase(log.getNomeUsuario() != null ? log.getNomeUsuario() : "SISTEMA", fontDados));
                table.addCell(new Phrase(log.getAcaoTipo().toString(), fontDados));
                table.addCell(new Phrase(log.getEntidadeNome() + " (ID: " + log.getEntidadeId() + ")", fontDados));
                
                // Trunca o JSON de detalhes se for muito grande para não quebrar o layout
                String detalhes = log.getAlteracoes() != null ? log.getAlteracoes() : "-";
                if (detalhes.length() > 50) detalhes = detalhes.substring(0, 47) + "...";
                table.addCell(new Phrase(detalhes, fontDados));
            }

            document.add(table);
            document.close();

            // Prepara o arquivo para download
            String nomeArquivo = "auditoria_" + dataInicio + "_a_" + dataFim + ".pdf";
            this.arquivoPDF = DefaultStreamedContent.builder()
                .contentType("application/pdf")
                .name(nomeArquivo)
                .stream(() -> new ByteArrayInputStream(out.toByteArray()))
                .build();

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao gerar PDF: " + e.getMessage()));
        }
    }

    // Getters e Setters
    public List<AuditoriaLog> getLogs() { return logs; }
    public AuditoriaLog getLogSelecionado() { return logSelecionado; }
    public void setLogSelecionado(AuditoriaLog logSelecionado) { this.logSelecionado = logSelecionado; }
    public TipoAcao[] getTiposAcao() { return TipoAcao.values(); }
    
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public StreamedContent getArquivoPDF() { return arquivoPDF; }
}