(ns brickhack.core
  (:require [quil.core :as q]
            [quil.middleware :as middleware]))

(def body (.-body js/document))
(def w (.-clientWidth body))
(def h (.-clientHeight body))

                                        ; This-sketch custom code
(def palette
  {:name       "purple haze"
   :background [0 0 0]
   :colors     [[32 0 40]
                [82 15 125]
                [99 53 126]
                [102 10 150]
                [132 26 200]
                [165 32 250]
                [196 106 251]]})

(defn particle
  "Create a particle obj"
  [id]
  {:id        id
   :vx        1
   :vy        1
   :size      2
   :direction 0
   :x         (q/random w)
   :y         (q/random h)
   :color     (rand-nth (:colors palette))})

(defn position
  "Calculates the next position based on the current, the speed and a max."
  [current delta max]
  (mod (+ current delta) max))

(def noise-zoom
  "Scalar for the coordinates of noise generation"
  0.005)

(defn direction
  "Define a pseudo flow field"
  [x y]
  (* 2 Math/PI (q/noise (* x noise-zoom) (* y noise-zoom))))

(defn velocity
  "combine two velocities"
  [current new]
  (/ (+ current new) 2))

                                        ; Start of the sketch codes

(defn sketch-setup []
                                        ; Set color mode to HSB (HSV) instead of default RGB
  (q/color-mode :hsb)
  (q/no-stroke)
  (apply q/background (:background palette))
                                        ; Create 2000 particles at the start
  (map particle (range 0 2000)))

(defn sketch-update [particles]
  (map (fn [p]
         (assoc p
                :x (position (:x p) (:vx p) w)
                :y (position (:y p) (:vy p) h)
                :direction (direction (:x p) (:y p))
                :vx (velocity (:dx p) (Math/cos (:direction p)))
                :vy (velocity (:dy p) (Math/sin (:direction p)))))
       particles))

(defn sketch-draw [particles]
 ; (apply q/background (:background palette))
  (doseq [p particles]
    (apply q/fill (:color p))
    (q/ellipse (:x p) (:y p) (:size p) (:size p))))

(defn create [canvas]
  (q/sketch
   :host canvas
   :size [w h]
   :draw #'sketch-draw
   :setup #'sketch-setup
   :update #'sketch-update
   :middleware [middleware/fun-mode]
   :settings (fn []
               (q/random-seed 1234)
               (q/noise-seed 1234))))

(defonce sketch (create "sketch"))
