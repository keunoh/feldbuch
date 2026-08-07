<script setup>
import {computed, onMounted, onUnmounted, ref,} from "vue";

const KEYWORDS = [

  "knowledge/",
  "conversation/",
  "summary/",
  "spring/",
  "java/",
  "vue/",
  "docker/",
  "redis/",
  "mysql/",
  "querydsl/",
  "jpa/",
  "oauth2/",
  "jwt/",
  "markdown/",
  "batch/",
  "openai/",
  "prompt/",
  "repository/",
  "entity/",
  "service/",
  "controller/",
  "scheduler/",
  "api/",
  "clean-code/",
  "architecture/",
  "feldbuch/",
];

const visibleKeywords =
  ref([]);

let timer = null;

function shuffle(array) {

  const copy =
    [...array];

  for (
    let i = copy.length - 1;
    i > 0;
    i--
  ) {

    const j =
      Math.floor(
        Math.random() * (i + 1),
      );

    [
      copy[i],
      copy[j],
    ] = [
      copy[j],
      copy[i],
    ];
  }

  return copy;
}

function updateKeywords() {

  visibleKeywords.value =
    shuffle(KEYWORDS)
      .slice(0, 16);

}

const rows =
  computed(() => {

    const result = [];

    for (
      let i = 0;
      i < visibleKeywords.value.length;
      i += 4
    ) {

      result.push(
        visibleKeywords.value.slice(
          i,
          i + 4,
        ),
      );

    }

    return result;

  });

onMounted(() => {

  updateKeywords();

  timer =
    setInterval(
      updateKeywords,
      7000,
    );

});

onUnmounted(() => {

  clearInterval(
    timer,
  );

});
</script>

<template>

  <div class="background-watermark">

    <div class="watermark-title">
      FELDBUCH
    </div>

    <TransitionGroup
      name="keyword"
      tag="div"
      class="watermark-grid"
    >

    <span
      v-for="keyword in visibleKeywords"
      :key="keyword"
    >
      {{ keyword }}
    </span>

    </TransitionGroup>

  </div>

</template>

<style scoped>

.background-watermark {

  position: fixed;

  inset: 0;

  display: flex;

  flex-direction: column;

  justify-content: center;

  align-items: center;

  pointer-events: none;

  user-select: none;

  z-index: 1;

}

.watermark-title {

  margin-bottom: 40px;

  font-size: clamp(140px, 16vw, 260px);

  font-family: var(--font-family-terminal);

  font-weight: 900;

  letter-spacing: .28em;

  color: rgba(70, 255, 130, .018);

  text-shadow: var(--text-shadow-watermark);

}

.watermark-grid {

  display: grid;

  grid-template-columns:
repeat(4, auto);

  gap: var(--space-8) 40px;

}

.watermark-grid span {

  font-family: var(--font-family-terminal);

  font-size: 13px;

  letter-spacing: .08em;

  color: rgba(70, 255, 130, .05);

  transition: opacity .8s,
  transform .8s;

}

.keyword-enter-active,
.keyword-leave-active {

  transition: all .8s;

}

.keyword-enter-from {

  opacity: 0;

  transform: translateY(8px);

}

.keyword-leave-to {

  opacity: 0;

  transform: translateY(-8px);

}

</style>
