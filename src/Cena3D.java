import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Painel que desenha a cena 3D do Problema 88: as duas cargas fixas, o
 * ponto C e os elementos de apoio (eixos, malha do chão, linhas guia).
 *
 * Toda a "engine" 3D é feita manualmente com Graphics2D — sem bibliotecas
 * externas — projetando pontos do espaço (Ponto3D) para a tela através
 * da {@link Camera3D}. A interação do usuário (rotacionar, dar zoom e
 * mover a cena) é tratada diretamente pelos listeners de mouse abaixo.
 */
public class Cena3D extends JPanel {

    private static final long serialVersionUID = 1L;

    private final transient Camera3D camera = new Camera3D();

    private double dMetros = 0.02;       // distância atual entre as cargas
    private double raioCargaMundo = 0.0026; // raio visual das esferas (não é o tamanho físico real)

    private boolean mostrarEixos = true;
    private boolean mostrarMalha = true;

    // --- animação da terceira carga vindo "do infinito" até C ---
    private boolean animando = false;
    private boolean cargaChegouEmC = false;
    private double progressoAnimacao = 0;
    private Timer timerAnimacao;
    private transient Runnable aoConcluirAnimacao;

    // --- controle de mouse (rotação / zoom / pan) ---
    private int ultimoX, ultimoY;

    public Cena3D() {
        setOpaque(true);
        setBackground(Paleta.FUNDO_CENA);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ultimoX = e.getX();
                ultimoY = e.getY();
            }
        };
        addMouseListener(mouse);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - ultimoX;
                int dy = e.getY() - ultimoY;
                ultimoX = e.getX();
                ultimoY = e.getY();

                if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
                    camera.pan(dx, dy);
                } else {
                    camera.rotacionar(dx * 0.012, dy * 0.009);
                }
                repaint();
            }
        });

        addMouseWheelListener((MouseWheelEvent e) -> {
            double fator = e.getWheelRotation() > 0 ? 1.12 : 1 / 1.12;
            camera.aplicarZoom(fator);
            repaint();
        });
    }

    /** Atualiza a distância entre as cargas (em metros) usada para montar a geometria da cena. */
    public void definirDistancia(double dMetros) {
        if (Math.abs(this.dMetros - dMetros) > 1e-12) {
            this.dMetros = dMetros;
            cargaChegouEmC = false; // a animação anterior não corresponde mais à nova geometria
            pararAnimacao();
        }
        repaint();
    }

    public void definirEixosVisiveis(boolean visivel) {
        this.mostrarEixos = visivel;
        repaint();
    }

    public void definirMalhaVisivel(boolean visivel) {
        this.mostrarMalha = visivel;
        repaint();
    }

    public boolean isCargaChegouEmC() {
        return cargaChegouEmC;
    }

    public void vistaPadrao() {
        camera.vistaPadrao();
        repaint();
    }

    public void resetarTudo() {
        camera.resetarCompleto();
        cargaChegouEmC = false;
        pararAnimacao();
        repaint();
    }

    private void pararAnimacao() {
        animando = false;
        progressoAnimacao = 0;
        if (timerAnimacao != null) {
            timerAnimacao.stop();
        }
    }

    /** Inicia a animação da carga q3 "vindo do infinito" até o ponto C. */
    public void iniciarAnimacao(Runnable aoConcluir) {
        if (animando) {
            return;
        }
        this.aoConcluirAnimacao = aoConcluir;
        cargaChegouEmC = false;
        animando = true;
        progressoAnimacao = 0;

        timerAnimacao = new Timer(16, null);
        timerAnimacao.addActionListener(e -> {
            progressoAnimacao += 0.016 / 1.1; // ~1.1s de duração total
            if (progressoAnimacao >= 1.0) {
                progressoAnimacao = 1.0;
                animando = false;
                cargaChegouEmC = true;
                timerAnimacao.stop();
                if (aoConcluirAnimacao != null) {
                    aoConcluirAnimacao.run();
                }
            }
            repaint();
        });
        timerAnimacao.start();
    }

    // ----------------------------------------------------------------
    // Geometria da cena (em metros, no referencial do mundo)
    // ----------------------------------------------------------------

    private Ponto3D pontoQ1() {
        return new Ponto3D(-dMetros / 2.0, 0, 0);
    }

    private Ponto3D pontoQ2() {
        return new Ponto3D(dMetros / 2.0, 0, 0);
    }

    private Ponto3D pontoMedio() {
        return new Ponto3D(0, 0, 0);
    }

    private Ponto3D pontoC() {
        return new Ponto3D(0, 0, dMetros / 2.0);
    }

    private double extentMalha() {
        return Math.max(0.022, dMetros * 1.3);
    }

    // ----------------------------------------------------------------
    // Renderização
    // ----------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int largura = getWidth();
        int altura = getHeight();

        g2.setPaint(new GradientPaint(0, 0, new Color(0xFC, 0xFD, 0xFF), 0, altura, new Color(0xE9, 0xEC, 0xF2)));
        g2.fillRect(0, 0, largura, altura);

        if (mostrarMalha) {
            desenharMalha(g2, largura, altura);
        }
        if (mostrarEixos) {
            desenharEixos(g2, largura, altura);
        }
        desenharGuias(g2, largura, altura);
        desenharObjetos(g2, largura, altura);
        desenharBarraCoordenadas(g2, largura);

        g2.dispose();
    }

    private void desenharMalha(Graphics2D g2, int largura, int altura) {
        double extent = extentMalha();
        int divisoes = 8;
        double passo = (extent * 2) / divisoes;

        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= divisoes; i++) {
            double v = -extent + i * passo;
            boolean central = Math.abs(v) < 1e-9;
            g2.setColor(central ? new Color(0xC7, 0xCD, 0xD9) : new Color(0xE2, 0xE6, 0xED));

            Camera3D.Projecao a1 = camera.projetar(new Ponto3D(v, -extent, 0), largura, altura);
            Camera3D.Projecao a2 = camera.projetar(new Ponto3D(v, extent, 0), largura, altura);
            desenharLinha(g2, a1, a2);

            Camera3D.Projecao b1 = camera.projetar(new Ponto3D(-extent, v, 0), largura, altura);
            Camera3D.Projecao b2 = camera.projetar(new Ponto3D(extent, v, 0), largura, altura);
            desenharLinha(g2, b1, b2);
        }
    }

    private void desenharEixos(Graphics2D g2, int largura, int altura) {
        double extent = extentMalha() * 1.15;
        desenharEixo(g2, largura, altura, new Ponto3D(extent, 0, 0), Paleta.EIXO_X, "x");
        desenharEixo(g2, largura, altura, new Ponto3D(0, extent, 0), Paleta.EIXO_Y, "y");
        desenharEixo(g2, largura, altura, new Ponto3D(0, 0, extent), Paleta.EIXO_Z, "z");
    }

    private void desenharEixo(Graphics2D g2, int largura, int altura, Ponto3D ponta, Color cor, String rotulo) {
        Ponto3D origem = new Ponto3D(0, 0, 0);
        Camera3D.Projecao pOrigem = camera.projetar(origem, largura, altura);
        Camera3D.Projecao pPonta = camera.projetar(ponta, largura, altura);
        if (pOrigem == null || pPonta == null) {
            return;
        }
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(cor);
        g2.draw(new Line2D.Double(pOrigem.x, pOrigem.y, pPonta.x, pPonta.y));

        desenharSetaNaPonta(g2, pOrigem, pPonta, cor);

        g2.setFont(Paleta.FONTE_SUBTITULO);
        g2.setColor(cor);
        g2.drawString(rotulo, (float) pPonta.x + 8, (float) pPonta.y + 4);
    }

    private void desenharSetaNaPonta(Graphics2D g2, Camera3D.Projecao origem, Camera3D.Projecao ponta, Color cor) {
        double ang = Math.atan2(ponta.y - origem.y, ponta.x - origem.x);
        double tam = 8;
        GeneralPath seta = new GeneralPath();
        seta.moveTo(ponta.x, ponta.y);
        seta.lineTo(ponta.x - tam * Math.cos(ang - Math.PI / 7), ponta.y - tam * Math.sin(ang - Math.PI / 7));
        seta.lineTo(ponta.x - tam * Math.cos(ang + Math.PI / 7), ponta.y - tam * Math.sin(ang + Math.PI / 7));
        seta.closePath();
        g2.setColor(cor);
        g2.fill(seta);
    }

    private void desenharGuias(Graphics2D g2, int largura, int altura) {
        Ponto3D q1 = pontoQ1();
        Ponto3D q2 = pontoQ2();
        Ponto3D m = pontoMedio();
        Ponto3D c = pontoC();

        float[] tracejado = {6f, 5f};
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4, tracejado, 0));
        g2.setColor(new Color(0x55, 0x5B, 0x66));

        desenharSegmento(g2, largura, altura, q1, m);
        desenharSegmento(g2, largura, altura, m, q2);
        desenharSegmento(g2, largura, altura, m, c);

        g2.setFont(Paleta.FONTE_LABEL);
        g2.setColor(new Color(0x33, 0x39, 0x44));
        Camera3D.Projecao pq1 = camera.projetar(q1, largura, altura);
        Camera3D.Projecao pq2 = camera.projetar(q2, largura, altura);
        Camera3D.Projecao pc = camera.projetar(c, largura, altura);
        double cxCentroide = 0, cyCentroide = 0;
        int n = 0;
        for (Camera3D.Projecao pp : new Camera3D.Projecao[]{pq1, pq2, pc}) {
            if (pp != null) {
                cxCentroide += pp.x;
                cyCentroide += pp.y;
                n++;
            }
        }
        if (n > 0) {
            cxCentroide /= n;
            cyCentroide /= n;
        }
        desenharRotuloNoMeio(g2, largura, altura, q1, m, "d/2", cxCentroide, cyCentroide);
        desenharRotuloNoMeio(g2, largura, altura, m, q2, "d/2", cxCentroide, cyCentroide);
        desenharRotuloNoMeio(g2, largura, altura, m, c, "d/2", cxCentroide, cyCentroide);
    }

    private void desenharSegmento(Graphics2D g2, int largura, int altura, Ponto3D a, Ponto3D b) {
        Camera3D.Projecao pa = camera.projetar(a, largura, altura);
        Camera3D.Projecao pb = camera.projetar(b, largura, altura);
        desenharLinha(g2, pa, pb);
    }

    private void desenharRotuloNoMeio(Graphics2D g2, int largura, int altura, Ponto3D a, Ponto3D b, String texto,
                                       double cxCentroide, double cyCentroide) {
        Camera3D.Projecao pa = camera.projetar(a, largura, altura);
        Camera3D.Projecao pb = camera.projetar(b, largura, altura);
        if (pa == null || pb == null) {
            return;
        }
        double midX = (pa.x + pb.x) / 2.0;
        double midY = (pa.y + pb.y) / 2.0;

        double dirX = pb.x - pa.x;
        double dirY = pb.y - pa.y;
        double comprimento = Math.sqrt(dirX * dirX + dirY * dirY);
        if (comprimento < 1e-6) {
            comprimento = 1;
        }
        double perpX = -dirY / comprimento;
        double perpY = dirX / comprimento;

        // garante que o rótulo fique do lado de fora do triângulo (longe do centroide)
        double paraCentroideX = midX - cxCentroide;
        double paraCentroideY = midY - cyCentroide;
        if (perpX * paraCentroideX + perpY * paraCentroideY < 0) {
            perpX = -perpX;
            perpY = -perpY;
        }

        double deslocamento = 13;
        double labelX = midX + perpX * deslocamento;
        double labelY = midY + perpY * deslocamento;

        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(texto);
        g2.drawString(texto, (float) (labelX - w / 2.0), (float) (labelY + fm.getAscent() / 2.0 - 2));
    }

    private void desenharLinha(Graphics2D g2, Camera3D.Projecao a, Camera3D.Projecao b) {
        if (a == null || b == null) {
            return;
        }
        g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
    }

    /** Representa um objeto esférico a ser desenhado, já com profundidade calculada, para ordenação. */
    private static class ObjetoEsferico {
        Ponto3D centro;
        double raioMundo;
        Color corClara;
        Color corEscura;
        boolean comSinal;
        String rotulo;
        boolean comSombra;

        ObjetoEsferico(Ponto3D centro, double raioMundo, Color corClara, Color corEscura, boolean comSinal, String rotulo, boolean comSombra) {
            this.centro = centro;
            this.raioMundo = raioMundo;
            this.corClara = corClara;
            this.corEscura = corEscura;
            this.comSinal = comSinal;
            this.rotulo = rotulo;
            this.comSombra = comSombra;
        }
    }

    private void desenharObjetos(Graphics2D g2, int largura, int altura) {
        List<ObjetoEsferico> objetos = new ArrayList<>();

        objetos.add(new ObjetoEsferico(pontoQ1(), raioCargaMundo, Paleta.CARGA_POSITIVA_CLARO, Paleta.CARGA_POSITIVA_ESCURO, true, "q\u2081", false));
        objetos.add(new ObjetoEsferico(pontoQ2(), raioCargaMundo, Paleta.CARGA_POSITIVA_CLARO, Paleta.CARGA_POSITIVA_ESCURO, true, "q\u2082", false));

        if (cargaChegouEmC) {
            objetos.add(new ObjetoEsferico(pontoC(), raioCargaMundo * 0.92, Paleta.CARGA_POSITIVA_CLARO, Paleta.CARGA_POSITIVA_ESCURO, true, "C (q\u2083)", true));
        } else if (animando) {
            double t = 1 - Math.pow(1 - progressoAnimacao, 3); // ease-out
            Ponto3D inicio = new Ponto3D(0, 0, pontoC().z + extentMalha() * 6);
            Ponto3D fim = pontoC();
            Ponto3D atual = new Ponto3D(
                    inicio.x + (fim.x - inicio.x) * t,
                    inicio.y + (fim.y - inicio.y) * t,
                    inicio.z + (fim.z - inicio.z) * t
            );
            objetos.add(new ObjetoEsferico(atual, raioCargaMundo * 0.92, Paleta.CARGA_POSITIVA_CLARO, Paleta.CARGA_POSITIVA_ESCURO, true, "q\u2083", false));
            objetos.add(new ObjetoEsferico(pontoC(), raioCargaMundo * 0.32, Paleta.PONTO_C_CLARO, Paleta.PONTO_C_ESCURO, false, "C", false));
        } else {
            objetos.add(new ObjetoEsferico(pontoC(), raioCargaMundo * 0.32, Paleta.PONTO_C_CLARO, Paleta.PONTO_C_ESCURO, false, "C", true));
        }

        List<double[]> entradas = new ArrayList<>(); // [indice, profundidade]
        List<Camera3D.Projecao> projecoes = new ArrayList<>();
        List<ObjetoEsferico> validos = new ArrayList<>();

        for (ObjetoEsferico o : objetos) {
            Camera3D.Projecao p = camera.projetar(o.centro, largura, altura);
            if (p == null) {
                continue;
            }
            validos.add(o);
            projecoes.add(p);
        }

        // ordena do mais distante para o mais próximo (pintor)
        List<Integer> ordem = new ArrayList<>();
        for (int i = 0; i < validos.size(); i++) ordem.add(i);
        ordem.sort(Comparator.comparingDouble(i -> -projecoes.get(i).profundidade));

        for (int idx : ordem) {
            ObjetoEsferico o = validos.get(idx);
            Camera3D.Projecao p = projecoes.get(idx);
            if (o.comSombra) {
                desenharSombra(g2, largura, altura, o);
            }
            double raioTela = o.raioMundo * camera.escalaNaProfundidade(p.profundidade);
            desenharEsfera(g2, p, raioTela, o.corClara, o.corEscura, o.comSinal);
            desenharRotuloAcima(g2, p, raioTela, o.rotulo);
        }
    }

    private void desenharSombra(Graphics2D g2, int largura, int altura, ObjetoEsferico o) {
        Ponto3D pe = new Ponto3D(o.centro.x, o.centro.y, 0);
        Camera3D.Projecao p = camera.projetar(pe, largura, altura);
        if (p == null) {
            return;
        }
        double raioTela = o.raioMundo * camera.escalaNaProfundidade(p.profundidade) * 1.15;
        g2.setColor(new Color(0, 0, 0, 45));
        g2.fill(new Ellipse2D.Double(p.x - raioTela, p.y - raioTela * 0.4, raioTela * 2, raioTela * 0.8));
    }

    private void desenharEsfera(Graphics2D g2, Camera3D.Projecao p, double raioTela, Color corClara, Color corEscura, boolean comSinal) {
        if (raioTela < 1) {
            raioTela = 1;
        }
        float cx = (float) p.x;
        float cy = (float) p.y;
        float r = (float) raioTela;

        Ellipse2D corpo = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);

        RadialGradientPaint gradiente = new RadialGradientPaint(
                cx - r * 0.35f, cy - r * 0.4f, r * 1.7f,
                new float[]{0f, 0.6f, 1f},
                new Color[]{aclarar(corClara, 0.55f), corClara, corEscura}
        );
        g2.setPaint(gradiente);
        g2.fill(corpo);

        g2.setStroke(new BasicStroke(Math.max(1f, r * 0.06f)));
        g2.setColor(corEscura);
        g2.draw(corpo);

        if (comSinal && r > 5) {
            g2.setColor(Color.WHITE);
            float espessura = Math.max(1.4f, r * 0.18f);
            float tam = r * 0.85f;
            g2.setStroke(new BasicStroke(espessura, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(cx - tam / 2, cy, cx + tam / 2, cy));
            g2.draw(new Line2D.Float(cx, cy - tam / 2, cx, cy + tam / 2));
        }
    }

    private Color aclarar(Color base, float fator) {
        int r = (int) Math.min(255, base.getRed() + (255 - base.getRed()) * fator);
        int g = (int) Math.min(255, base.getGreen() + (255 - base.getGreen()) * fator);
        int b = (int) Math.min(255, base.getBlue() + (255 - base.getBlue()) * fator);
        return new Color(r, g, b);
    }

    private void desenharRotuloAcima(Graphics2D g2, Camera3D.Projecao p, double raioTela, String texto) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        g2.setFont(Paleta.FONTE_SUBTITULO);
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(texto);
        float x = (float) (p.x - w / 2.0);
        float y = (float) (p.y - raioTela - 8);
        g2.setColor(Color.WHITE);
        g2.drawString(texto, x + 1, y + 1);
        g2.setColor(Paleta.TEXTO_PRIMARIO);
        g2.drawString(texto, x, y);
    }

    private void desenharBarraCoordenadas(Graphics2D g2, int largura) {
        Ponto3D c = pontoC();
        String texto = String.format(java.util.Locale.US,
                "C  \u2192  x: %.3f m    y: %.3f m    z: %.3f m", c.x, c.y, c.z);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(texto) + 28;
        int h = 28;
        int x = (largura - w) / 2;
        int y = 14;

        RoundRectangle2D fundo = new RoundRectangle2D.Double(x, y, w, h, 14, 14);
        g2.setColor(new Color(0x12, 0x2B, 0x5C, 230));
        g2.fill(fundo);

        g2.setColor(Color.WHITE);
        g2.drawString(texto, x + 14, y + h - 9);
    }
}
