(ns brickhack.core
  (:require [quil.core :as q]
            [quil.middleware :as middleware]))

(def body (.-body js/document))
(def w (.-clientWidth body))
(def h (.-clientHeight body))

                                        ; This-sketch custom code
(def palettes
  [{:name       "white on blue"
   :background [228 91 20]
   :colors     [[198 47 83]
                [198 25 86]
                [184 22 99]
                [190 10 90]
                [1   91 80]
                [190 0  93]
                [184 32 52]
                [185 23 60]
                [196 3  60]]}
   {:name       "salmon and blue"
   :background [58 5 100]
   :colors     [[354 74  70]
                [354 64 100]
                [200 74  80]
                [200 69  70]
                [1   78  75]
                [58  20 100]
                [1   64 100]
                [205 100 80]
                [205 50  80]]}
   {:name       "sepia and blue"
   :background [221 7 98]
   :colors     [[50  89  75]
                [50  33 100]
                [50  43  95]
                [242 100 69]
                [242 43  99]
                [232 70  80]
                [232 40  90]]}
   {:name       "pale green and brick"
   :background [139 7 100]
   :colors     [[139 35 70]
                [139 15 90]
                [139 25 100]
                [1 55 70]
                [1 85 70]
                [1 25 100]]}])

(def palette (rand-nth palettes))

(defn particle
  "Create a particle obj"
  [id]
  {:id        id
   :vx        0
   :vy        0
   :size      2
   :direction 0
   :length    0
   :x         (q/random w)
   :y         (q/random h)
   :color     (rand-nth (:colors palette))})


(def noise-zoom 0.005)

(defn noise-field
  "Generate a perlin noise location dependent value"
  ([x y]      (noise-field x y noise-zoom))
  ([x y zoom] (q/noise (* x zoom) (* y zoom))))

(defn noise-field-radian
  "Get a position dependent radian"
  ([x y]      (* 2 Math/PI (noise-field x y noise-zoom)))
  ([x y zoom] (* 2 Math/PI (noise-field x y zoom))))

(defn update-size 
  "Line sizing function"
  [current new]
  (/ (+ current new) 2))

(defn update-position
  "Calculates the next position based on the current, the speed and a max."
  [current delta max]
  (mod (+ current delta) max))

(defn update-velocity
  "combine two velocities"
  [current new]
  (/ (+ current new) 2))

                                        ; Start of the sketch codes

(defn sketch-setup []
                                        ; Set color mode to HSB (HSV) instead of default RGB
  (q/color-mode :hsb 360 100 100 1.0)
  (q/no-stroke)
  (apply q/background (:background palette))
                                        ; Create 2000 particles at the start
  (map particle (range 0 2000)))

(defn sketch-update [particles]
  (map (fn [p]
         (assoc p
                :x (update-position (:x p) (:vx p) w)
                :y (update-position (:y p) (:vy p) h)
                :length (+ 1 (:length p))
                :direction (noise-field-radian (:x p) (:y p))
                :vx (update-velocity (:dx p) (Math/cos (:direction p)))
                :vy (update-velocity (:dy p) (Math/sin (:direction p)))))
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
