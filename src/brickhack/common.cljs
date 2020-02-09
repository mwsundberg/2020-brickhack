(ns brickhack.common
  (:require [quil.core :as q]))

(defn coords-with-radian
  [x y r rScalar]
  [(+ x (* rScalar (Math/cos r)))
   (+ y (* rScalar (Math/sin r)))])

(defn add-with-rollover
  "Calculates the next position based on the current, the speed and a max."
  [current delta max]
  (mod (+ current delta) max))

(defn average
  "Used for updating velocities and sizes"
  [current new]
  (/ (+ current new) 2))