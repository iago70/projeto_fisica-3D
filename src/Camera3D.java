/**
 * Câmera orbital usada para visualizar a cena 3D.
 *
 * A câmera fica sempre apontada para um ponto-alvo ({@code alvo}) e orbita
 * ao seu redor de acordo com dois ângulos: azimute (rotação horizontal) e
 * elevação (inclinação vertical). A distância controla o zoom e o alvo
 * pode ser deslocado para realizar o "pan" (arrastar com o botão direito).
 *
 * A convenção de eixos usada no mundo é: X para a direita, Y para dentro
 * da tela (profundidade) e Z para cima — igual a softwares de CAD/engenharia.
 */
public class Camera3D {

    /** Resultado da projeção de um ponto 3D para coordenadas de tela 2D. */
    public static class Projecao {
        public final double x;
        public final double y;
        public final double profundidade; // usado para ordenação pintor (maior = mais longe)

        Projecao(double x, double y, double profundidade) {
            this.x = x;
            this.y = y;
            this.profundidade = profundidade;
        }
    }

    private static final double ELEVACAO_MIN = Math.toRadians(8);
    private static final double ELEVACAO_MAX = Math.toRadians(85);
    private static final double DISTANCIA_MIN = 0.04;
    private static final double DISTANCIA_MAX = 0.50;

    private static final double AZIMUTE_PADRAO = Math.toRadians(-35);
    private static final double ELEVACAO_PADRAO = Math.toRadians(24);
    private static final double DISTANCIA_PADRAO = 0.095;

    private double azimute = AZIMUTE_PADRAO;
    private double elevacao = ELEVACAO_PADRAO;
    private double distancia = DISTANCIA_PADRAO;
    private Ponto3D alvo = new Ponto3D(0, 0.006, 0.013);

    /** Distância focal (em pixels) usada na projeção em perspectiva. */
    private final double focal = 760;

    public void rotacionar(double deltaAzimuteRad, double deltaElevacaoRad) {
        azimute -= deltaAzimuteRad;
        elevacao += deltaElevacaoRad;
        if (elevacao < ELEVACAO_MIN) elevacao = ELEVACAO_MIN;
        if (elevacao > ELEVACAO_MAX) elevacao = ELEVACAO_MAX;
    }

    public void aplicarZoom(double fatorMultiplicativo) {
        distancia *= fatorMultiplicativo;
        if (distancia < DISTANCIA_MIN) distancia = DISTANCIA_MIN;
        if (distancia > DISTANCIA_MAX) distancia = DISTANCIA_MAX;
    }

    /** Move o alvo da câmera no plano da tela (direita/cima), para o efeito de "pan". */
    public void pan(double deltaPixelsX, double deltaPixelsY) {
        double[] r = direitaCamera();
        double[] u = cimaCamera();
        // quanto mais perto (zoom), menor o deslocamento em metros por pixel
        double escala = distancia / focal;
        double dx = (-deltaPixelsX * r[0] + deltaPixelsY * u[0]) * escala;
        double dy = (-deltaPixelsX * r[1] + deltaPixelsY * u[1]) * escala;
        double dz = (-deltaPixelsX * r[2] + deltaPixelsY * u[2]) * escala;
        alvo = new Ponto3D(alvo.x + dx, alvo.y + dy, alvo.z + dz);
    }

    public void vistaPadrao() {
        azimute = AZIMUTE_PADRAO;
        elevacao = ELEVACAO_PADRAO;
        distancia = DISTANCIA_PADRAO;
    }

    public void resetarCompleto() {
        vistaPadrao();
        alvo = new Ponto3D(0, 0.006, 0.013);
    }

    private double[] posicaoCamera() {
        double cx = alvo.x + distancia * Math.cos(elevacao) * Math.sin(azimute);
        double cy = alvo.y - distancia * Math.cos(elevacao) * Math.cos(azimute);
        double cz = alvo.z + distancia * Math.sin(elevacao);
        return new double[]{cx, cy, cz};
    }

    private double[] frenteCamera() {
        double[] cam = posicaoCamera();
        double fx = alvo.x - cam[0];
        double fy = alvo.y - cam[1];
        double fz = alvo.z - cam[2];
        return normalizar(fx, fy, fz);
    }

    private double[] direitaCamera() {
        double[] f = frenteCamera();
        // worldUp = (0,0,1)
        double rx = f[1] * 1 - f[2] * 0;
        double ry = f[2] * 0 - f[0] * 1;
        double rz = f[0] * 0 - f[1] * 0;
        return normalizar(rx, ry, rz);
    }

    private double[] cimaCamera() {
        double[] f = frenteCamera();
        double[] r = direitaCamera();
        double ux = r[1] * f[2] - r[2] * f[1];
        double uy = r[2] * f[0] - r[0] * f[2];
        double uz = r[0] * f[1] - r[1] * f[0];
        return normalizar(ux, uy, uz);
    }

    private double[] normalizar(double x, double y, double z) {
        double n = Math.sqrt(x * x + y * y + z * z);
        if (n < 1e-9) return new double[]{0, 0, 0};
        return new double[]{x / n, y / n, z / n};
    }

    /** Fator de escala (pixels por metro) numa determinada profundidade — usado para
     *  calcular o raio aparente de esferas e a espessura de linhas na tela. */
    public double escalaNaProfundidade(double profundidade) {
        return focal / profundidade;
    }

    /**
     * Projeta um ponto do mundo 3D para coordenadas de tela, considerando
     * o tamanho do painel (para centralizar a projeção).
     */
    public Projecao projetar(Ponto3D pontoMundo, int largura, int altura) {
        double[] cam = posicaoCamera();
        double[] f = frenteCamera();
        double[] r = direitaCamera();
        double[] u = cimaCamera();

        double vx = pontoMundo.x - cam[0];
        double vy = pontoMundo.y - cam[1];
        double vz = pontoMundo.z - cam[2];

        double cz = vx * f[0] + vy * f[1] + vz * f[2]; // profundidade (frente da câmera)
        if (cz < 0.005) {
            return null; // atrás do plano próximo da câmera
        }
        double cx = vx * r[0] + vy * r[1] + vz * r[2];
        double cy = vx * u[0] + vy * u[1] + vz * u[2];

        double escala = focal / cz;
        double telaX = largura / 2.0 + cx * escala;
        double telaY = altura / 2.0 - cy * escala;
        return new Projecao(telaX, telaY, cz);
    }
}
