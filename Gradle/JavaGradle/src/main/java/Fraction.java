import java.io.*;
/**
 * Purpose: demonstrate simple Java Fraction class with command line,
 * jdb debugging, and Ant build file.
 *
 * Ser321 Foundations of Distributed Applications
 * see http://pooh.poly.asu.edu/Ser321
 * @author Tim Lindquist Tim.Lindquist@asu.edu
 *         Software Engineering, CIDSE, IAFSE, ASU Poly
 * @version January 2020
 */
public class Fraction {

   private int numerator, denominator;

   public Fraction(){
      numerator = denominator = 0;
   }

   public void print() {
    System.out.print(numerator + "/" + denominator );
   }

   public void setNumerator (int n ){
      numerator = n;
   }

   public void setDenominator (int d) {
      denominator = d;
   }

   public int getDenominator() {
      return denominator;
   }

   public int getNumerator() {
      return numerator;
   }

   public static void main (String args[]) {
      
      int num, denom;
      
      try {
         // create a new instance
         // Fraction *frac = [[Fraction alloc] init];
         Fraction frac = new Fraction();

         try {
            if (args[0] != null) {
               num = Integer.parseInt(args[0]);
            }
            if (args[1] != null) {
               denom = Integer.parseInt(args[1]);
            }
         } catch (NumberFormatException nfe) {
            System.out.println("Arguments must be an integers!");
            System.exit(2);
         }
         
         // set the default values
         frac.setNumerator(1);
         frac.setDenominator(3);
         
         // change values if argument
         if (num != null) {
            frac.setNumerator(num);
         }
         if (denom != null) {
            frac.setDenominator(denom);
         }

         // print it
         System.out.print("The fraction is: ");
         frac.print();
         System.out.println("");

      }catch(Exception e) {
         e.printStackTrace();
      }
   }
}

