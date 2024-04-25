/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.util;

public class FastRandom extends java.util.Random {

    private static final long serialVersionUID = 1L;

    final double REAL_UNIT_INT = 1.0 / (0x7FFFFFFFL);
    final double REAL_UNIT_UINT = 1.0 / (0xFFFFFFFFL);
    final long Y = 842502087L, Z = 3579807591L, W = 273326509L;
    final double TWOPI = Math.PI * 2;
    final double LOG4 = Math.log(4);
    final double SG_MAGICCONST = 1.0 + Math.log(4.5);
    long x, y, z, w;
    long boolBuffer;
    int boolBufferBits = 0;
    double gaussNext;
    boolean hasGaussNext;

    public FastRandom() {
        seed((int) System.currentTimeMillis());
    }

    @Override
    public void setSeed(long seed) {
        seed((int) seed);
    }

    public void seed(int seed) {
        // The only stipulation stated for the xorshift RNG is that at least one of
        // the seeds x,y,z,w is non-zero. We fulfill that requirement by only allowing
        // resetting of the x seed
        x = seed;
        y = Y;
        z = Z;
        w = W;
    }

    @Override
    public boolean nextBoolean() {
        if (boolBufferBits == 0) {
            boolBuffer = nextUInt();
            boolBufferBits = 32;
        }
        boolBuffer >>= 1;
        boolean bit = (boolBuffer & 1) == 0;
        --boolBufferBits;
        return bit;
    }

    @Override
    public void nextBytes(byte[] buffer) {
        // Fill up the bulk of the buffer in chunks of 4 bytes at a time.
        long x = this.x, y = this.y, z = this.z, w = this.w;
        int i = 0;
        long t;
        for (int bound = buffer.length - 3; i < bound; ) {
            // Generate 4 bytes.
            // Increased performance is achieved by generating 4 random bytes per loop.
            // Also note that no mask needs to be applied to zero out the higher order bytes before
            // casting because the cast ignores thos bytes. Thanks to Stefan Trosch黷z for pointing this out.
            t = (x ^ (x << 11));
            x = y;
            y = z;
            z = w;
            w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));

            buffer[i++] = (byte) w;
            buffer[i++] = (byte) (w >> 8);
            buffer[i++] = (byte) (w >> 16);
            buffer[i++] = (byte) (w >> 24);
        }

        // Fill up any remaining bytes in the buffer.
        if (i < buffer.length) {
            // Generate 4 bytes.
            t = (x ^ (x << 11));
            x = y;
            y = z;
            z = w;
            w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));

            buffer[i++] = (byte) w;
            if (i < buffer.length) {
                buffer[i++] = (byte) (w >> 8);
                if (i < buffer.length) {
                    buffer[i++] = (byte) (w >> 16);
                    if (i < buffer.length) {
                        buffer[i] = (byte) (w >> 24);
                    }
                }
            }
        }
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    public double nextDouble() {
        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;

        // Here we can gain a 2x speed improvement by generating a value that can be cast to
        // an int instead of the more easily available uint. If we then explicitly cast to an
        // int the compiler will then cast the int to a double to perform the multiplication,
        // this final cast is a lot faster than casting from a uint to a double. The extra cast
        // to an int is very fast (the allocated bits remain the same) and so the overall effect
        // of the extra cast is a significant performance improvement.
        //
        // Also note that the loss of one bit of precision is equivalent to what occurs within
        // System.Random.
        return (REAL_UNIT_INT * (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8)))));
    }

    public double random() {
        return nextDouble();
    }

    @Override
    public float nextFloat() {
        return (float) nextDouble();
    }

    @Override
    public int nextInt() {
        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;
        return (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))));
    }

    @Override
    public int nextInt(int upperBound) {
        if (upperBound < 0)
            throw new IllegalArgumentException("upperBound must be >=0");

        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;

        return (int) ((REAL_UNIT_INT * (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))))) * upperBound);
    }

    public int nextInt(int lowerBound, int upperBound) {
        if (lowerBound > upperBound)
            throw new IllegalArgumentException("upperBound must be >=lowerBound");

        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;

        // The explicit int cast before the first multiplication gives better performance.
        // See comments in NextDouble.
        int range = upperBound - lowerBound;
        if (range < 0) {
            // If range is <0 then an overflow has occured and must resort to using long integer arithmetic instead (slower).
            // We also must use all 32 bits of precision, instead of the normal 31, which again is slower.
            return lowerBound + (int) ((REAL_UNIT_UINT * (double) (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8)))) * (double) ((long) upperBound - (long) lowerBound));
        }
        // 31 bits of precision will suffice if range<=int.MaxValue. This allows us to cast to an int and gain
        // a little more performance.
        return lowerBound + (int) ((REAL_UNIT_INT * (double) (int) (0x7FFFFFFF & (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))))) * (double) range);
    }

    public long nextUInt() {
        long t = (x ^ (x << 11));
        x = y;
        y = z;
        z = w;
        return (w = (w ^ (w >> 19)) ^ (t ^ (t >> 8))) & (0xFFFFFFFFL);
    }

    @Override
    public long nextLong() {
        return nextUInt() << 32 + nextUInt();
    }

    /**
     * Get a random number in the range [min, max) or [min, max] depending on rounding.
     *
     * @param min Low bound
     * @param max High bound
     * @return A uniformly distributed double
     */
    public double uniform(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Triangular distribution.
     * <p/>
     * Continuous distribution bounded by given lower and upper limits,
     * and having a given mode value in-between.
     * http://en.wikipedia.org/wiki/Triangular_distribution
     *
     * @param low  Low bound
     * @param high High bound
     * @param mode Mode
     * @return A number from the triangular distribution specified
     */
    public double triangular(int low, int high, int mode) {
        double u = nextDouble();
        double c = (mode - low) / (high - low);
        if (u > c) {
            u = 1.0 - u;
            c = 1.0 - c;
            int k = low;
            low = high;
            high = k;
        }
        return low + (high - low) * Math.sqrt(u * c);
    }

    /**
     * Gaussian distribution, mean is 0 and standard deviation is 1.
     * <p/>
     * mu is the mean, and sigma is the standard deviation.
     *
     * @return A double in Gaussian distribution
     */
    public double gauss() {
        return nextGaussian();
    }

    /**
     * Gaussian distribution, with user-specified mean and standard deviation.
     * <p/>
     * mu is the mean, and sigma is the standard deviation.
     *
     * @return A double in Gaussian distribution
     */
    public double gauss(double mu, double sigma) {
        return mu + sigma * nextGaussian();
    }

    public double gaussUnsigned(double mu, double sigma) {
        double out = gauss(mu, sigma);
        return out > 1 ? out : 1;
    }

    /**
     * Log normal distribution.
     * <p/>
     * If you take the natural logarithm of this distribution, you'll get a
     * normal distribution with mean mu and standard deviation sigma.
     * mu can have any value, and sigma must be greater than zero.
     *
     * @param mu    Mean
     * @param sigma Standard deviation
     * @return A number from the log normal distribution specified
     */
    public double logNormal(double mu, double sigma) {
        return Math.exp(gauss(mu, sigma));
    }

    /**
     * Exponential distribution.
     * <p/>
     * lambda is 1.0 divided by the desired mean.  It should be
     * nonzero. Returned values range from 0 to positive infinity
     * if lambda is positive, and from negative infinity to 0
     * if lambda is negative.
     *
     * @param lambda A non-zero value
     */
    public double exponential(double lambda) {
        return -Math.log(1.0 - random()) / lambda;
    }

    /**
     * Circular data distribution.
     * <p/>
     * If kappa is equal to zero, this distribution reduces
     * to a uniform random angle over the range 0 to 2*pi.
     *
     * @param mu    the mean angle, expressed in radians between 0 and 2*pi.
     * @param kappa the concentration parameter, which must be greater than or
     *              equal to zero.
     * @return A number from the circular data distribution specified
     */
    public double circularData(double mu, double kappa) {
        if (kappa <= 1e-6)
            return TWOPI * nextDouble();

        double a = 1.0 + Math.sqrt(1.0 + 4.0 * kappa * kappa);
        double b = (a - Math.sqrt(2.0 * a)) / (2.0 * kappa);
        double r = (1.0 + b * b) / (2.0 * b);
        double u1, u2, u3, f, c, z, theta = 0;

        while (true) {
            u1 = nextDouble();

            z = Math.cos(Math.PI * u1);
            f = (1.0 + r * z) / (r + z);
            c = kappa * (r - f);

            u2 = nextDouble();

            if (u2 < c * (2.0 - c) || u2 <= c * Math.exp(1.0 - c))
                break;

            u3 = nextDouble();
            if (u3 > 0.5)
                theta = (mu % TWOPI) + Math.acos(f);
            else
                theta = (mu % TWOPI) - Math.acos(f);
        }
        return theta;
    }

    /**
     * Gamma distribution.  Not the gamma function!
     * Conditions on the parameters are alpha > 0 and beta > 0.
     * <p/>
     * The probability distribution function is:
     * pdf(x) = (x ** (alpha - 1) * math.exp(-x / beta)) / (math.gamma(alpha) * beta ** alpha)
     *
     * @param alpha Alpha
     * @param beta  Beta
     * @return A number from the gamma distribution specified
     */
    public double gamma(double alpha, double beta) {
        if (alpha <= 0.0 || beta <= 0.0)
            throw new IllegalArgumentException("alpha and beta must be > 0.0");

        if (alpha > 1.0) {
            double ainv = Math.sqrt(2.0 * alpha - 1.0);
            double bbb = alpha - LOG4;
            double ccc = alpha + ainv;
            double u1, u2, v, x, z, r;

            while (true) {
                u1 = random();
                if (!(1e-7 < u1 && u1 < .9999999))
                    continue;
                u2 = 1.0 - random();
                v = Math.log(u1 / (1.0 - u1)) / ainv;
                x = alpha * Math.exp(v);
                z = u1 * u1 * u2;
                r = bbb + ccc * v - x;
                if (r + SG_MAGICCONST - 4.5 * z >= 0.0 || r >= Math.log(z))
                    return x * beta;
            }
        } else if (alpha == 1.0) {
            // exponential(1)
            double u;
            u = random();
            while (u <= 1e-7)
                u = random();
            return -Math.log(u) * beta;
        } else {
            // alpha is between 0 and 1 (exclusive)
            // Uses ALGORITHM GS of Statistical Computing -Kennedy & Gentle

            double u, b, p, x, u1;
            while (true) {
                u = random();
                b = (Math.E + alpha) / Math.E;
                p = b * u;
                if (p <= 1.0)
                    x = Math.pow(p, (1.0 / alpha));
                else
                    x = -Math.log((b - p) / alpha);
                u1 = random();
                if (p > 1.0) {
                    if (u1 <= Math.pow(x, (alpha - 1.0)))
                        break;
                } else if (u1 <= Math.exp(-x))
                    break;
            }
            return x * beta;
        }
    }
}