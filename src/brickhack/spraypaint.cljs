(ns brickhack.spraypaint
  (:require [brickhack.common :as c]
    	    [quil.core :as q]
            [quil.middleware :as middleware]))

(def body (.-body js/document))
(def w (.-clientWidth body))
(def h (.-clientHeight body))

                                        ; This-sketch custom code
(def palette (rand-nth c/palettes))
(defn particle [id] (assoc (c/particle id w h palette) :size 1))

(def noise-zoom 0.005)
(def velocity-scalar 10)

(defn noise-field
  "Generate a perlin noise location dependent value"
  ([x y]      (noise-field x y noise-zoom))
  ([x y zoom] (q/noise (* x zoom) (* y zoom))))

(defn noise-field-radian
  "Get a position dependent radian"
  ([x y]      (noise-field-radian x y noise-zoom 4))
  ([x y zoom] (noise-field-radian x y zoom 4))
  ([x y zoom scalar] (* scalar Math/PI (noise-field x y zoom))))

(defn noise-scalar
  "Get a line-consistent scalar value for random noise"
  [x y i]
  (* 10 (q/noise x y (* i 0.0025))))
(defn noise-field-color
  [x y i]
  (nth (:colors palette) (c/normalize-to (c/noise-field x y (* 1.5 noise-zoom) (/ i 1000)) (count (:colors palette)))))

                                        ; Start of the sketch codes

(defn sketch-setup []
                                        ; Set color mode to HSB (HSV) instead of default RGB
  (q/color-mode :hsb 360 100 100 1.0)
  (q/no-stroke)
  (apply q/background (:background palette))
                                        ; Create 2000 particles at the start
  ; (render-field w h)
  (q/no-stroke)
  (map particle (range 0 50000)))

(defn sketch-update [particles]
  (map (fn [p]
         (assoc p
                :x (c/add-with-rollover (:x p) (* velocity-scalar (:vx p)) w)
                :y (c/add-with-rollover (:y p) (* velocity-scalar (:vy p)) h)
                :length (+ 1 (:length p))
                :direction (noise-field-radian (:x p) (:y p))
                :vx (c/average (:dx p) (Math/cos (:direction p)))
                :vy (c/average (:dy p) (Math/sin (:direction p)))))
       particles))

(defn sketch-draw [particles]
 ; (apply q/background (:background palette))
  (doseq [p particles]
    (apply q/fill (:color p))
    (dotimes [i 4]
      (q/ellipse (+ (* (q/random-gaussian) (noise-scalar (:x p) (:y p) (:id p))) (:x p))
                 (+ (* (q/random-gaussian) (noise-scalar (:x p) (:y p) (:id p))) (:y p))
                 (:size p)
                 (:size p)))))

(defn create [canvas]
  (q/sketch
   :host canvas
   :size [w h]
   :draw #'sketch-draw
   :setup #'sketch-setup
   :update #'sketch-update
   :middleware [middleware/fun-mode]
   :settings (fn []
               (q/random-seed 765)
               (q/noise-seed 234))))

(defonce sketch (create "sketch"))
