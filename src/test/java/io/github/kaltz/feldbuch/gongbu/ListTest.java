package io.github.kaltz.feldbuch.gongbu;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListTest {

    @Test
    void arrayList는_요소_추가와_수정이_가능하다() {
        List<Number> arrayList = new ArrayList<>();

        arrayList.add(1);
        arrayList.add(2);
        arrayList.add(3);
        arrayList.set(0, 5);

        assertThat(arrayList).containsExactly(5, 2, 3);
    }

    @Test
    void arraysAsList는_요소_수정은_가능하지만_추가와_삭제는_불가능하다() {
        List<Number> asList = Arrays.asList(1, 2, 3);

        asList.set(0, 5);

        assertThat(asList).containsExactly(5, 2, 3);
        assertThatThrownBy(() -> asList.add(4))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> asList.remove(0))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void listOf는_수정할_수_없는_리스트를_만든다() {
        List<Number> listOf = List.of(1, 2, 3);

        assertThat(listOf).containsExactly(1, 2, 3);
        assertThatThrownBy(() -> listOf.add(4))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> listOf.set(0, 5))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
