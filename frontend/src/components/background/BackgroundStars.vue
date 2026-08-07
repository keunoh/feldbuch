<script setup>
import {onMounted, onUnmounted, ref,} from "vue";

const canvas =
  ref(null);

let context;

let animationId;

let stars = [];

let shootingStars = [];

const STAR_COUNT = 280;

const colors = [

  "#42ff8d",
  "#53ffa2",
  "#8affc4",
  "#c9ffe6"

];

class Star {

  constructor(
    width,
    height,
  ) {
    this.reset(
      width,
      height,
    );
  }

  reset(
    width,
    height,
  ) {

    this.x =
      Math.random() * width;

    this.y =
      Math.random() * height;

    this.radius =
      Math.random() * 2.6 + 0.3;

    this.alpha =
      Math.random() * 0.7 + 0.15;

    this.speed =
      Math.random() * 0.02 + 0.004;

    this.direction =
      Math.random() > 0.5
        ? 1
        : -1;
  }

  update() {

    this.alpha +=
      this.speed *
      this.direction;

    if (
      this.alpha > 1
    ) {
      this.alpha = 1;
      this.direction = -1;
    }

    if (
      this.alpha < 0.12
    ) {
      this.alpha = 0.12;
      this.direction = 1;
    }
  }

  draw() {

    context.beginPath();

    context.fillStyle =
      this.color =
        colors[
          Math.floor(
            Math.random() * colors.length
          )
          ];

    context.shadowBlur =
      8;

    context.shadowColor =
      "#41ff8e";

    context.arc(
      this.x,
      this.y,
      this.radius,
      0,
      Math.PI * 2,
    );

    context.fill();

    context.shadowBlur = 0;
  }
}

class ShootingStar {

  constructor(
    width,
  ) {

    this.x =
      width + 100;

    this.y =
      Math.random() * 420 + 120;

    this.length =
      Math.random() * 220 + 120;

    this.speed =
      Math.random() * 15 + 12;

    this.opacity = 1;
  }

  update() {

    this.x -=
      this.speed;

    this.y +=
      this.speed * 0.35;

    this.opacity -=
      0.004;
  }

  draw() {

    context.save();

    context.translate(
      this.x,
      this.y,
    );

    context.rotate(
      -0.45,
    );

    const gradient =
      context.createLinearGradient(
        0,
        0,
        this.length,
        0,
      );

    gradient.addColorStop(
      0,
      `rgba(255,255,255,${this.opacity})`,
    );

    gradient.addColorStop(
      0.25,
      `rgba(91,255,166,${this.opacity})`,
    );

    gradient.addColorStop(
      1,
      "transparent",
    );

    context.strokeStyle =
      gradient;

    context.lineWidth =
      2;

    context.beginPath();

    context.moveTo(
      0,
      0,
    );

    context.lineTo(
      this.length,
      0,
    );

    context.stroke();

    context.restore();
  }
}

function resizeCanvas() {

  canvas.value.width =
    window.innerWidth;

  canvas.value.height =
    window.innerHeight;
}

function animate() {

  const width =
    canvas.value.width;

  const height =
    canvas.value.height;

  context.clearRect(
    0,
    0,
    width,
    height,
  );

  stars.forEach(star => {

    star.update();

    star.draw();

  });

  shootingStars.forEach(star => {

    star.update();

    star.draw();

  });

  shootingStars =
    shootingStars.filter(
      star =>
        star.opacity > 0,
    );

  if (
    Math.random() < 0.002
  ) {

    shootingStars.push(
      new ShootingStar(
        width,
      ),
    );

  }

  animationId =
    requestAnimationFrame(
      animate,
    );
}

onMounted(() => {

  context =
    canvas.value.getContext(
      "2d",
    );

  resizeCanvas();

  stars =
    Array.from(
      {
        length:
        STAR_COUNT,
      },
      () =>
        new Star(
          canvas.value.width,
          canvas.value.height,
        ),
    );

  animate();

  window.addEventListener(
    "resize",
    resizeCanvas,
  );

});

onUnmounted(() => {

  cancelAnimationFrame(
    animationId,
  );

  window.removeEventListener(
    "resize",
    resizeCanvas,
  );

});
</script>

<template>

  <canvas
    ref="canvas"
    class="background-stars"
  />

</template>

<style scoped>

.background-stars {

  position: fixed;

  inset: 0;

  z-index: 0;

  pointer-events: none;

}

</style>
