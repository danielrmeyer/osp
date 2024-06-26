/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * ODE defines a minimal differential equation solver.
 * @author       Wolfgang Christian
 */
public interface ODESolver {
  /**
   * Initializes the ODE solver.
   *
   * ODE solvers use this method to allocate temporary arrays that may be required to carry out the solution.
   * The number of differential equations is determined by invoking getState().length on the ODE.
   *
   * @param stepSize
   */
  public void initialize(double stepSize);

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getRate method to obtain the initial state of the system.
   * The ODESolver then advances the solution and copies the new state into the
   * state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step();

  /**
   * Sets the initial step size.
   *
   * The step size may change if the ODE solver implements an adaptive step size algorithm
   * such as RK4/5.
   *
   * @param stepSize
   */
  public void setStepSize(double stepSize);

  /**
   * Gets the step size.
   *
   * @return the step size
   */
  public double getStepSize();

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2024  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
