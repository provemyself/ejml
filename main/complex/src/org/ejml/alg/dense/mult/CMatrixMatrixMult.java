/*
 * Copyright (c) 2009-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.alg.dense.mult;

import org.ejml.data.CDenseMatrix64F;
import org.ejml.ops.CCommonOps;

/**
 * @author Peter Abeles
 */
public class CMatrixMatrixMult {

    public static void mult_reorder( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        if( a.numCols == 0 || a.numRows == 0 ) {
            CCommonOps.fill(c,0,0);
            return;
        }
        double realA;
        double imgA;

        int indexCbase= 0;
        int strideA = a.getRowStride();
        int strideB = b.getRowStride();
        int strideC = c.getRowStride();
        int endOfKLoop = b.numRows*strideB;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*strideA;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + strideB;

            realA = a.data[indexA++];
            imgA = a.data[indexA++];

            while( indexB < end ) {
                double realB = b.data[indexB++];
                double imgB = b.data[indexB++];

                c.data[indexC++] = realA*realB - imgA*imgB;
                c.data[indexC++] = realA*imgB + imgA*realB;
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + strideB;

                realA = a.data[indexA++];
                imgA = a.data[indexA++];

                while( indexB < end ) { // j loop
                    double realB = b.data[indexB++];
                    double imgB = b.data[indexB++];

                    c.data[indexC++] += realA*realB - imgA*imgB;
                    c.data[indexC++] += realA*imgB + imgA*realB;
                }
            }
            indexCbase += strideC;
        }
    }

    public static void mult_small( CDenseMatrix64F a , CDenseMatrix64F b , CDenseMatrix64F c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
        }

        int aIndexStart = 0;
        int cIndex = 0;

        int strideA = a.getRowStride();
        int strideB = b.getRowStride();

        for( int i = 0; i < a.numRows; i++ ) {
            for( int j = 0; j < b.numCols; j++ ) {
                double realTotal = 0;
                double imgTotal = 0;

                int indexA = aIndexStart;
                int indexB = j*2;
                int end = indexA + strideA;
                while( indexA < end ) {
                    double realA = a.data[indexA++];
                    double imgA = a.data[indexA++];

                    double realB = b.data[indexB];
                    double imgB = b.data[indexB+1];

                    realTotal += realA*realB - imgA*imgB;
                    imgTotal += realA*imgB + imgA*realB;

                    indexB += strideB;
                }

                c.data[cIndex++] = realTotal;
                c.data[cIndex++] = imgTotal;
            }
            aIndexStart += strideA;
        }
    }
}