package me.skitttyy.kami.api.utils.render.animation;

public enum Easing
{
    LINEAR
            {
                @Override
                public double ease(double factor)
                {
                    return factor;
                }
            },
    SINE_IN
            {
                @Override
                public double ease(double factor)
                {
                    return 1 - Math.cos((factor * Math.PI) / 2);
                }
            },
    SINE_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return Math.sin((factor * Math.PI) / 2);
                }
            },
    SINE_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return -(Math.cos(Math.PI * factor) - 1) / 2;
                }
            },
    CUBIC_IN
            {
                @Override
                public double ease(double factor)
                {
                    return Math.pow(factor, 3);
                }
            },
    CUBIC_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return 1 - Math.pow(1 - factor, 3);
                }
            },
    CUBIC_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor < 0.5 ? 4 * Math.pow(factor, 3) : 1 - Math.pow(-2 * factor + 2, 3) / 2;
                }
            },
    QUAD_IN
            {
                @Override
                public double ease(double factor)
                {
                    return Math.pow(factor, 2);
                }
            },
    QUAD_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return 1 - (1 - factor) * (1 - factor);
                }
            },
    QUAD_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor < 0.5 ? 8 * Math.pow(factor, 4) : 1 - Math.pow(-2 * factor + 2, 4) / 2;
                }
            },
    QUART_IN
            {
                @Override
                public double ease(double factor)
                {
                    return Math.pow(factor, 4);
                }
            },
    QUART_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return 1 - Math.pow(1 - factor, 4);
                }
            },
    QUART_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor < 0.5 ? 8 * Math.pow(factor, 4) : 1 - Math.pow(-2 * factor + 2, 4) / 2;
                }
            },
    QUINT_IN
            {
                @Override
                public double ease(double factor)
                {
                    return Math.pow(factor, 5);
                }
            },
    QUINT_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return 1 - Math.pow(1 - factor, 5);
                }
            },
    QUINT_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor < 0.5 ? 16 * Math.pow(factor, 5) : 1 - Math.pow(-2 * factor + 2, 5) / 2;
                }
            },
    CIRC_IN
            {
                @Override
                public double ease(double factor)
                {
                    return 1 - Math.sqrt(1 - Math.pow(factor, 2));
                }
            },
    CIRC_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return Math.sqrt(1 - Math.pow(factor - 1, 2));
                }
            },
    CIRC_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * factor, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * factor + 2, 2)) + 1) / 2;
                }
            },
    EXPO_IN
            {
                @Override
                public double ease(double factor)
                {
                    return Math.min(0, Math.pow(2, 10 * factor - 10));
                }
            },
    EXPO_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return Math.max(1 - Math.pow(2, -10 * factor), 1);
                }
            },
    EXPO_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor == 0 ? 0 : factor == 1 ? 1 : factor < 0.5 ? Math.pow(2, 20 * factor - 10) / 2 : (2 - Math.pow(2, -20 * factor + 10)) / 2;
                }
            },
    ELASTIC_IN
            {
                @Override
                public double ease(double factor)
                {
                    return factor == 0 ? 0 : factor == 1 ? 1 : -Math.pow(2, 10 * factor - 10) * Math.sin((factor * 10 - 10.75) * ((2 * Math.PI) / 3));
                }
            },
    ELASTIC_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor == 0 ? 0 : factor == 1 ? 1 : Math.pow(2, -10 * factor) * Math.sin((factor * 10 - 0.75) * ((2 * Math.PI) / 3)) + 1;
                }
            },
    ELASTIC_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    double sin = Math.sin((20 * factor - 11.125) * ((2 * Math.PI) / 4.5));

                    return factor == 0 ? 0 : factor == 1 ? 1 : factor < 0.5 ? -(Math.pow(2, 20 * factor - 10) * sin) / 2 : (Math.pow(2, -20 * factor + 10) * sin) / 2 + 1;
                }
            },
    BACK_IN
            {
                @Override
                public double ease(double factor)
                {
                    return 2.70158 * Math.pow(factor, 3) - 1.70158 * factor * factor;
                }
            },
    BACK_OUT
            {
                @Override
                public double ease(double factor)
                {
                    double c1 = 1.70158;
                    double c3 = c1 + 1;

                    return 1 + c3 * Math.pow(factor - 1, 3) + c1 * Math.pow(factor - 1, 2);
                }
            },
    BACK_IN_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return factor < 0.5 ? (Math.pow(2 * factor, 2) * (((1.70158 * 1.525) + 1) * 2 * factor - (1.70158 * 1.525))) / 2 : (Math.pow(2 * factor - 2, 2) * (((1.70158 * 1.525) + 1) * (factor * 2 - 2) + (1.70158 * 1.525)) + 2) / 2;
                }
            },
    BOUNCE_IN
            {
                @Override
                public double ease(double factor)
                {
                    return 1 - Easing.bounceOut(1 - factor);
                }
            },
    BOUNCE_OUT
            {
                @Override
                public double ease(double factor)
                {
                    return Easing.bounceOut(factor);
                }
            },
    BOUNCE_IN_OUT
            {
                public double ease(double factor)
                {
                    return factor < 0.5 ? (1 - bounceOut(1 - 2 * factor)) / 2 : (1 + bounceOut(2 * factor - 1)) / 2;
                }
            };

    public abstract double ease(double factor);

    private static double bounceOut(double in)
    {
        double n1 = 7.5625;
        double d1 = 2.75;

        if (in < 1 / d1)
        {
            return n1 * in * in;
        }
        else if (in < 2 / d1)
        {
            return n1 * (in -= 1.5 / d1) * in + 0.75;
        }
        else if (in < 2.5 / d1)
        {
            return n1 * (in -= 2.25 / d1) * in + 0.9375;
        }
        else
        {
            return n1 * (in -= 2.625 / d1) * in + 0.984375;
        }
    }
}