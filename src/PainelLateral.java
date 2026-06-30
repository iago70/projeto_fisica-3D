import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Barra lateral com os parâmetros de entrada (carga e distância), os
 * resultados calculados (potencial, trabalho e energia) as informações
 * auxiliares e o controle de animação da terceira carga.
 *
 * Esta classe cuida da sua própria lógica de validação/cálculo, delegando
 * a geometria 3D para a {@link Cena3D} e avisando a janela principal sobre
 * mudanças de parâmetro através de {@link #definirOuvinteParametros}.
 */
public class PainelLateral extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Cena3D cena3D;

    private JTextField campoCarga;
    private JTextField campoDistancia;

    private JLabel valorPotencial;
    private JLabel valorTrabalho;
    private JLabel valorEnergia;

    private JLabel infoR;
    private JLabel infoAltura;

    private BotaoArredondado botaoAnimacao;
    private JLabel statusAnimacao;

    private JCheckBox checkEixos;
    private JCheckBox checkMalha;

    private transient BiConsumer<Double, Double> ouvinteParametros;
    private double ultimoQ = 2.0;
    private double ultimoD = 2.0;

    public PainelLateral(Cena3D cena3D) {
        this.cena3D = cena3D;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xE3, 0xE6, 0xEC)));
        setPreferredSize(new Dimension(320, 10));

        JPanel conteudo = new JPanel();
        conteudo.setOpaque(false);
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBorder(new EmptyBorder(16, 18, 16, 18));

        conteudo.add(tituloSecao("Par\u00e2metros"));
        conteudo.add(Box.createVerticalStrut(10));
        conteudo.add(linhaCampo("Carga el\u00e9trica (q\u2081 = q\u2082 = q\u2083) em \u03bcC:"));
        campoCarga = criarCampoTexto("2,0");
        conteudo.add(campoCarga);
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(linhaCampo("Dist\u00e2ncia entre as cargas d (cm):"));
        campoDistancia = criarCampoTexto("2,0");
        conteudo.add(campoDistancia);

        conteudo.add(Box.createVerticalStrut(12));
        JPanel linhaBotoesCalculo = new JPanel(new GridLayout(1, 2, 8, 0));
        linhaBotoesCalculo.setOpaque(false);
        linhaBotoesCalculo.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaBotoesCalculo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        BotaoArredondado botaoCalcular = new BotaoArredondado("Calcular", Paleta.VERDE, Paleta.VERDE_ESCURO);
        botaoCalcular.addActionListener(e -> calcularComValidacao());
        BotaoArredondado botaoLimpar = new BotaoArredondado("Limpar", Paleta.VERMELHO, Paleta.VERMELHO_ESCURO);
        botaoLimpar.addActionListener(e -> limparCampos());
        linhaBotoesCalculo.add(botaoCalcular);
        linhaBotoesCalculo.add(botaoLimpar);
        conteudo.add(linhaBotoesCalculo);

        conteudo.add(Box.createVerticalStrut(14));
        conteudo.add(separador());
        conteudo.add(Box.createVerticalStrut(12));

        conteudo.add(tituloSecao("Resultados"));
        conteudo.add(Box.createVerticalStrut(10));
        valorPotencial = new JLabel("\u2014");
        conteudo.add(cartaoResultado("(a) Potencial el\u00e9trico em C (V)", valorPotencial));
        conteudo.add(Box.createVerticalStrut(10));
        valorTrabalho = new JLabel("\u2014");
        conteudo.add(cartaoResultado("(b) Trabalho para trazer q\u2083 do infinito (J)", valorTrabalho));
        conteudo.add(Box.createVerticalStrut(10));
        valorEnergia = new JLabel("\u2014");
        conteudo.add(cartaoResultado("(c) Energia potencial da nova configura\u00e7\u00e3o (J)", valorEnergia));

        conteudo.add(Box.createVerticalStrut(14));
        conteudo.add(separador());
        conteudo.add(Box.createVerticalStrut(12));

        conteudo.add(tituloSecao("Informa\u00e7\u00f5es"));
        conteudo.add(Box.createVerticalStrut(8));
        JLabel infoK = infoLinha(envolverHtml("k (constante eletrost\u00e1tica): 9,0 \u00d7 10\u2079 N\u00b7m\u00b2/C\u00b2", 248));
        conteudo.add(infoK);
        conteudo.add(Box.createVerticalStrut(4));
        infoR = infoLinha(envolverHtml("Dist\u00e2ncia r (de cada carga at\u00e9 C): \u2014", 248));
        conteudo.add(infoR);
        conteudo.add(Box.createVerticalStrut(4));
        infoAltura = infoLinha(envolverHtml("Altura do ponto C: d/2 = \u2014", 248));
        conteudo.add(infoAltura);

        conteudo.add(Box.createVerticalStrut(14));
        conteudo.add(separador());
        conteudo.add(Box.createVerticalStrut(12));

        botaoAnimacao = new BotaoArredondado("\u25b6  Iniciar Anima\u00e7\u00e3o", Paleta.VERDE, Paleta.VERDE_ESCURO);
        botaoAnimacao.setAlignmentX(Component.LEFT_ALIGNMENT);
        botaoAnimacao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        botaoAnimacao.addActionListener(e -> iniciarAnimacao());
        conteudo.add(botaoAnimacao);

        conteudo.add(Box.createVerticalStrut(8));
        statusAnimacao = new JLabel("Traz a carga q\u2083 do infinito at\u00e9 C");
        statusAnimacao.setFont(Paleta.FONTE_INFO);
        statusAnimacao.setForeground(Paleta.TEXTO_SECUNDARIO);
        statusAnimacao.setAlignmentX(Component.LEFT_ALIGNMENT);
        conteudo.add(statusAnimacao);

        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(separador());
        conteudo.add(Box.createVerticalStrut(10));

        JPanel linhaChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaChecks.setOpaque(false);
        linhaChecks.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkEixos = criarCheckbox("Eixos");
        checkMalha = criarCheckbox("Malha");
        linhaChecks.add(checkEixos);
        linhaChecks.add(Box.createHorizontalStrut(18));
        linhaChecks.add(checkMalha);
        conteudo.add(linhaChecks);

        checkEixos.addActionListener(e -> cena3D.definirEixosVisiveis(checkEixos.isSelected()));
        checkMalha.addActionListener(e -> cena3D.definirMalhaVisivel(checkMalha.isSelected()));

        conteudo.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(conteudo,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        DocumentListener escuta = aoMudarTexto(this::recalcular);
        campoCarga.getDocument().addDocumentListener(escuta);
        campoDistancia.getDocument().addDocumentListener(escuta);

        recalcular();
    }

    public void definirOuvinteParametros(BiConsumer<Double, Double> ouvinte) {
        this.ouvinteParametros = ouvinte;
        if (ouvinte != null) {
            ouvinte.accept(ultimoQ, ultimoD);
        }
    }

    public void resetarValoresPadrao() {
        campoCarga.setText("2,0");
        campoDistancia.setText("2,0");
        checkEixos.setSelected(true);
        checkMalha.setSelected(true);
        statusAnimacao.setText("Traz a carga q\u2083 do infinito at\u00e9 C");
        statusAnimacao.setForeground(Paleta.TEXTO_SECUNDARIO);
        botaoAnimacao.setEnabled(true);
        botaoAnimacao.setText("\u25b6  Iniciar Anima\u00e7\u00e3o");
        recalcular();
    }

    private void iniciarAnimacao() {
        if (!botaoAnimacao.isEnabled()) {
            return;
        }
        botaoAnimacao.setEnabled(false);
        botaoAnimacao.setText("\u25b6  Animando...");
        statusAnimacao.setText("q\u2083 est\u00e1 vindo do infinito at\u00e9 C...");
        statusAnimacao.setForeground(Paleta.AZUL_CLARO);

        cena3D.iniciarAnimacao(() -> {
            botaoAnimacao.setEnabled(true);
            botaoAnimacao.setText("\u21bb  Reiniciar Anima\u00e7\u00e3o");
            statusAnimacao.setText("q\u2083 chegou ao ponto C  \u2713");
            statusAnimacao.setForeground(Paleta.VERDE_ESCURO);
        });
    }

    /** Calcula com a mesma valida\u00e7\u00e3o "passo a passo" (com di\u00e1logos) do c\u00f3digo
     *  original que deu origem a este projeto: avisa campos vazios, valores inv\u00e1lidos
     *  e confirma o sucesso do c\u00e1lculo. Os resultados j\u00e1 s\u00e3o atualizados em tempo
     *  real enquanto o usu\u00e1rio digita (veja {@link #recalcular()}); este bot\u00e3o serve
     *  para quem prefere o fluxo expl\u00edcito "preencher e clicar em Calcular". */
    private void calcularComValidacao() {
        String textoCarga = campoCarga.getText().trim();
        String textoDistancia = campoDistancia.getText().trim();

        if (textoCarga.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a carga el\u00e9trica.",
                    "Campo obrigat\u00f3rio", JOptionPane.WARNING_MESSAGE);
            campoCarga.requestFocus();
            return;
        }
        if (textoDistancia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a dist\u00e2ncia.",
                    "Campo obrigat\u00f3rio", JOptionPane.WARNING_MESSAGE);
            campoDistancia.requestFocus();
            return;
        }

        try {
            double q = Double.parseDouble(textoCarga.replace(",", "."));
            double d = Double.parseDouble(textoDistancia.replace(",", "."));

            if (q <= 0) {
                JOptionPane.showMessageDialog(this, "A carga deve ser maior que zero.",
                        "Valor inv\u00e1lido", JOptionPane.ERROR_MESSAGE);
                campoCarga.requestFocus();
                campoCarga.selectAll();
                return;
            }
            if (d <= 0) {
                JOptionPane.showMessageDialog(this, "A dist\u00e2ncia deve ser maior que zero.",
                        "Valor inv\u00e1lido", JOptionPane.ERROR_MESSAGE);
                campoDistancia.requestFocus();
                campoDistancia.selectAll();
                return;
            }

            recalcular();
            JOptionPane.showMessageDialog(this, "C\u00e1lculo realizado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Digite apenas n\u00fameros.\n\nExemplos v\u00e1lidos:\n2\n2,5\n2.5",
                    "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Limpa os campos de entrada (os resultados voltam a mostrar "\u2014" automaticamente,
     *  atrav\u00e9s do mesmo recalculo em tempo real disparado pelo DocumentListener). */
    private void limparCampos() {
        campoCarga.setText("");
        campoDistancia.setText("");
        campoCarga.requestFocus();
    }

    private void recalcular() {
        String textoCarga = campoCarga.getText().trim();
        String textoDistancia = campoDistancia.getText().trim();

        try {
            double q = Double.parseDouble(textoCarga.replace(",", "."));
            double d = Double.parseDouble(textoDistancia.replace(",", "."));

            Fisica.Resultado r = Fisica.calcular(q, d);

            valorPotencial.setText(formatarCientifico(r.potencialV) + " V");
            valorTrabalho.setText(String.format(Locale.US, "%.2f J", r.trabalhoW));
            valorEnergia.setText(String.format(Locale.US, "%.2f J", r.energiaU));

            infoR.setText(envolverHtml("Dist\u00e2ncia r (de cada carga at\u00e9 C): "
                    + String.format(Locale.US, "%.5f m", r.distanciaR), 248));
            infoAltura.setText(envolverHtml("Altura do ponto C: d/2 = "
                    + String.format(Locale.US, "%.4f m", r.alturaC), 248));

            ultimoQ = q;
            ultimoD = d;
            cena3D.definirDistancia(r.dMetros);

            botaoAnimacao.setEnabled(!cena3D.isCargaChegouEmC());
            if (!cena3D.isCargaChegouEmC()) {
                botaoAnimacao.setText("\u25b6  Iniciar Anima\u00e7\u00e3o");
                statusAnimacao.setText("Traz a carga q\u2083 do infinito at\u00e9 C");
                statusAnimacao.setForeground(Paleta.TEXTO_SECUNDARIO);
            }

            if (ouvinteParametros != null) {
                ouvinteParametros.accept(q, d);
            }
        } catch (NumberFormatException ex) {
            valorPotencial.setText("\u2014");
            valorTrabalho.setText("\u2014");
            valorEnergia.setText("\u2014");
            infoR.setText(envolverHtml("Dist\u00e2ncia r (de cada carga at\u00e9 C): \u2014", 248));
            infoAltura.setText(envolverHtml("Altura do ponto C: d/2 = \u2014", 248));
        } catch (IllegalArgumentException ex) {
            valorPotencial.setText("valor inv\u00e1lido");
            valorTrabalho.setText("valor inv\u00e1lido");
            valorEnergia.setText("valor inv\u00e1lido");
        }
    }

    /** Formata em nota\u00e7\u00e3o cient\u00edfica (ex.: "2.55 \u00d7 10\u2076") calculando a mantissa e o
     *  expoente diretamente, sem depender de formatar e depois reanalisar o texto \u2014
     *  evitando assim qualquer problema com v\u00edrgula/ponto de localidades regionais
     *  como pt-BR (onde String.format poderia gerar "2,55" e quebrar o parse). */
    private String formatarCientifico(double valor) {
        if (valor == 0) {
            return "0.00 \u00d7 10\u2070";
        }
        int expoente = (int) Math.floor(Math.log10(Math.abs(valor)));
        double mantissa = valor / Math.pow(10, expoente);

        // corrige eventuais erros de arredondamento de log10 (ex.: 9.999 -> expoente errado)
        if (Math.round(Math.abs(mantissa) * 100) >= 1000) {
            mantissa /= 10;
            expoente += 1;
        } else if (Math.abs(mantissa) < 1) {
            mantissa *= 10;
            expoente -= 1;
        }

        String mantissaTexto = String.format(Locale.US, "%.2f", mantissa);
        String exp = expoenteSobrescrito(expoente);
        return mantissaTexto + " \u00d7 10" + exp;
    }

    private String expoenteSobrescrito(int expoente) {
        String digitos = String.valueOf(Math.abs(expoente));
        String[] sobrescritos = {"\u2070", "\u00b9", "\u00b2", "\u00b3", "\u2074", "\u2075", "\u2076", "\u2077", "\u2078", "\u2079"};
        StringBuilder sb = new StringBuilder();
        if (expoente < 0) {
            sb.append("\u207b");
        }
        for (char c : digitos.toCharArray()) {
            sb.append(sobrescritos[c - '0']);
        }
        return sb.toString();
    }

    // ----------------------------------------------------------------
    // Componentes auxiliares de UI
    // ----------------------------------------------------------------

    private String envolverHtml(String texto, int larguraPx) {
        return envolverHtmlComFonte(texto, larguraPx, Paleta.FONTE_INFO);
    }

    /** Quebra o texto manualmente em linhas que cabem em larguraMaxPx (medido com a fonte dada)
     *  e devolve um HTML pronto para um JLabel — mais confi\u00e1vel do que o truque
     *  "body style='width:...'", que pode falhar dependendo da largura final alocada ao componente. */
    private String envolverHtmlComFonte(String texto, int larguraMaxPx, Font fonte) {
        BufferedImage imagemTemporaria = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagemTemporaria.createGraphics();
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();

        StringBuilder html = new StringBuilder("<html>");
        StringBuilder linhaAtual = new StringBuilder();
        for (String palavra : texto.split(" ")) {
            String tentativa = linhaAtual.length() == 0 ? palavra : linhaAtual + " " + palavra;
            if (fm.stringWidth(tentativa) > larguraMaxPx && linhaAtual.length() > 0) {
                html.append(linhaAtual).append("<br>");
                linhaAtual = new StringBuilder(palavra);
            } else {
                linhaAtual = new StringBuilder(tentativa);
            }
        }
        html.append(linhaAtual).append("</html>");
        g2.dispose();
        return html.toString();
    }

    private JLabel tituloSecao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        l.setForeground(Paleta.AZUL_PRINCIPAL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel linhaCampo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Paleta.FONTE_LABEL);
        l.setForeground(Paleta.TEXTO_PRIMARIO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    private JLabel infoLinha(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Paleta.FONTE_INFO);
        l.setForeground(Paleta.TEXTO_SECUNDARIO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField criarCampoTexto(String valorInicial) {
        JTextField campo = new JTextField(valorInicial);
        campo.setFont(Paleta.FONTE_CAMPO);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        Border borda = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC9, 0xCE, 0xD8), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );
        campo.setBorder(borda);
        return campo;
    }

    private JCheckBox criarCheckbox(String texto) {
        JCheckBox c = new JCheckBox(texto, true);
        c.setOpaque(false);
        c.setFont(Paleta.FONTE_LABEL);
        c.setForeground(Paleta.TEXTO_PRIMARIO);
        c.setFocusPainted(false);
        return c;
    }

    private Component separador() {
        JPanel linha = new JPanel();
        linha.setBackground(new Color(0xE6, 0xE9, 0xEF));
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        linha.setPreferredSize(new Dimension(10, 1));
        return linha;
    }

    private Cartao cartaoResultado(String rotulo, JLabel valor) {
        Cartao cartao = new Cartao(Paleta.RESULTADO_FUNDO, Paleta.RESULTADO_BORDA, 10);
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));
        cartao.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JLabel lblRotulo = new JLabel(envolverHtml(rotulo, 222));
        lblRotulo.setFont(Paleta.FONTE_INFO);
        lblRotulo.setForeground(Paleta.TEXTO_SECUNDARIO);
        lblRotulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        valor.setFont(Paleta.FONTE_RESULTADO);
        valor.setForeground(Paleta.AZUL_PRINCIPAL);
        valor.setAlignmentX(Component.LEFT_ALIGNMENT);
        valor.setHorizontalAlignment(SwingConstants.LEFT);

        cartao.add(lblRotulo);
        cartao.add(Box.createVerticalStrut(4));
        cartao.add(valor);
        return cartao;
    }

    private interface AcaoTexto {
        void executar();
    }

    private DocumentListener aoMudarTexto(AcaoTexto acao) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                acao.executar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                acao.executar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                acao.executar();
            }
        };
    }
}
