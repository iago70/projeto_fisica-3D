/**
 * Lógica física do Problema 88 (Halliday): duas cargas iguais e fixas,
 * separadas por uma distância d, e um ponto C acima do ponto médio entre
 * elas, a uma altura d/2.
 *
 * Esta classe não depende de Swing nem de nenhuma biblioteca gráfica —
 * contém apenas as fórmulas de eletrostática, o que facilita reaproveitar
 * ou testar o cálculo separadamente da interface.
 */
public final class Fisica {

    /** Constante eletrostática, em N·m²/C². */
    public static final double K = 9.0e9;

    private Fisica() {
        // classe utilitária, não deve ser instanciada
    }

    /** Resultado completo do cálculo, já pronto para ser exibido na interface. */
    public static class Resultado {
        public final double qCoulombs;     // carga em Coulombs
        public final double dMetros;       // distância entre q1 e q2, em metros
        public final double alturaC;       // altura do ponto C acima do ponto médio (d/2), em metros
        public final double distanciaR;    // distância de cada carga até C, em metros
        public final double potencialV;    // potencial elétrico em C, em Volts
        public final double trabalhoW;     // trabalho para trazer q3 do infinito até C, em Joules
        public final double energiaU;      // energia potencial da configuração final, em Joules

        Resultado(double qCoulombs, double dMetros, double alturaC, double distanciaR,
                  double potencialV, double trabalhoW, double energiaU) {
            this.qCoulombs = qCoulombs;
            this.dMetros = dMetros;
            this.alturaC = alturaC;
            this.distanciaR = distanciaR;
            this.potencialV = potencialV;
            this.trabalhoW = trabalhoW;
            this.energiaU = energiaU;
        }
    }

    /**
     * Calcula o potencial em C, o trabalho para trazer uma terceira carga
     * idêntica do infinito até C, e a energia potencial final do sistema.
     *
     * @param qMicroCoulombs carga de cada uma das três cargas (q1 = q2 = q3), em microcoulombs
     * @param dCentimetros   distância entre as duas cargas fixas, em centímetros
     */
    public static Resultado calcular(double qMicroCoulombs, double dCentimetros) {
        if (qMicroCoulombs <= 0) {
            throw new IllegalArgumentException("A carga deve ser maior que zero.");
        }
        if (dCentimetros <= 0) {
            throw new IllegalArgumentException("A distância deve ser maior que zero.");
        }

        double q = qMicroCoulombs * 1e-6;
        double d = dCentimetros / 100.0;

        double alturaC = d / 2.0;
        // r = distância do ponto médio até C ao quadrado (d/2) + (d/2), por Pitágoras:
        double r = Math.sqrt((d / 2.0) * (d / 2.0) + alturaC * alturaC); // = d/raiz(2)

        double v = 2 * K * q / r;          // potencial das duas cargas somado em C
        double w = q * v;                  // trabalho = q3 * V (energia ganha pela carga ao chegar)
        double u = K * q * q * (1.0 / d + 2.0 / r); // energia potencial total da nova configuração

        return new Resultado(q, d, alturaC, r, v, w, u);
    }
}
