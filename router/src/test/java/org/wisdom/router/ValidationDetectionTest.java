package org.wisdom.router;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.Email;
import org.junit.Before;
import org.junit.Test;
import org.wisdom.api.annotations.Body;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Status;
import org.wisdom.api.router.RouteUtils;
import org.wisdom.test.parents.Action;
import org.wisdom.test.parents.Invocation;

import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.wisdom.test.parents.Action.action;

/**
 * Check the validation detection.
 */
public class ValidationDetectionTest {

    private RequestRouter router;

    @Before
    public void setUp() {
        router = new RequestRouter();
        router.setValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    public void testValidationRequiredWithOneParameter() {
        FakeController controller = new FakeController() {
            @Route(method = HttpMethod.GET, uri = "/")
            public Result index(@NotNull @Parameter("name") String name) {
                return ok();
            }
        };

        router.bindController(controller);

        final org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
        assertThat(route).isNotNull();
        assertThat(route.getArguments()).hasSize(1);
        assertThat(route.getArguments().get(0).getName()).isEqualToIgnoringCase("name");
        assertThat(route.getArguments().get(0).getSource()).isEqualTo(RouteUtils.Source.PARAMETER);
        assertThat(route.getArguments().get(0).getType()).isEqualTo(String.class);

        // Valid invocation
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "wisdom").invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);

        // Invalid invocation
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

    }


    @Test
    public void testValidationWithoutValidator() {
        router.setValidator(null);

        FakeController controller = new FakeController() {
            @Route(method = HttpMethod.GET, uri = "/")
            public Result index(@NotNull @Parameter("name") String name) {
                return ok();
            }
        };

        router.bindController(controller);

        final org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
        assertThat(route).isNotNull();
        assertThat(route.getArguments()).hasSize(1);
        assertThat(route.getArguments().get(0).getName()).isEqualToIgnoringCase("name");
        assertThat(route.getArguments().get(0).getSource()).isEqualTo(RouteUtils.Source.PARAMETER);
        assertThat(route.getArguments().get(0).getType()).isEqualTo(String.class);

        // Invalid invocation
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).invoke();

        // No validator.
        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);
    }

    @Test
    public void testValidationRequiredWithSeveralParameters() {
        FakeController controller = new FakeController() {
            @Route(method = HttpMethod.GET, uri = "/")
            public Result index(@NotNull @Parameter("name") String name, @Email @NotNull @Parameter("email") String
                    email, @Parameter("i") int i) {
                return ok();
            }
        };

        router.bindController(controller);

        final org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
        assertThat(route).isNotNull();
        // Valid invocation
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "wisdom").parameter("email", "wisdom@w.io").parameter("i", 0).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);

        // Invalid invocation
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("email", "wisdom@w.io").parameter("i", 0).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

    }

    @Test
    public void testValidationRequiredWithOneParameterWithConstraintAfter() {
        FakeController controller = new FakeController() {
            @Route(method = HttpMethod.GET, uri = "/")
            public Result index(@Parameter("name") @NotNull String name) {
                return ok();
            }
        };

        router.bindController(controller);

        final org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
        assertThat(route).isNotNull();
        assertThat(route.getArguments()).hasSize(1);
        assertThat(route.getArguments().get(0).getName()).isEqualToIgnoringCase("name");
        assertThat(route.getArguments().get(0).getSource()).isEqualTo(RouteUtils.Source.PARAMETER);
        assertThat(route.getArguments().get(0).getType()).isEqualTo(String.class);

        // Valid invocation
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "wisdom").invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);

        // Invalid invocation
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

    }

    @Test
    public void testValidationRequiredWithOneParameterWithHibernateConstraint() {
        FakeController controller = new FakeController() {
            @Route(method = HttpMethod.GET, uri = "/")
            public Result index(@Parameter("name") @NotNull @Email String name) {
                return ok();
            }
        };

        router.bindController(controller);

        final org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
        assertThat(route).isNotNull();
        assertThat(route.getArguments()).hasSize(1);
        assertThat(route.getArguments().get(0).getName()).isEqualToIgnoringCase("name");
        assertThat(route.getArguments().get(0).getSource()).isEqualTo(RouteUtils.Source.PARAMETER);
        assertThat(route.getArguments().get(0).getType()).isEqualTo(String.class);

        // Valid invocation
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "wisdom@wisdom.com").invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);

        // Invalid invocation (no email)
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

        // Invalid invocation (bad email)
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "wisdom_is_not_an_email").invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

    }

    @Test
    public void testValidationRequiredWithOneParameterWithOnlyHibernateConstraint() {
        FakeController controller = new FakeController() {
            @Route(method = HttpMethod.GET, uri = "/")
            public Result index(@Parameter("name") @Email String name) {
                return ok();
            }
        };

        router.bindController(controller);

        final org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
        assertThat(route).isNotNull();
        assertThat(route.getArguments()).hasSize(1);
        assertThat(route.getArguments().get(0).getName()).isEqualToIgnoringCase("name");
        assertThat(route.getArguments().get(0).getSource()).isEqualTo(RouteUtils.Source.PARAMETER);
        assertThat(route.getArguments().get(0).getType()).isEqualTo(String.class);

        // Valid invocation
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "wisdom@wisdom.com").invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);

        // Valid invocation even without email
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);

        // Invalid invocation (broken email)
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "this-is-not an email").invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

        // Invalid invocation (bad email)
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).parameter("name", "wisdom_is_not_an_email").invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

    }

    @Test
    public void testValidationRequiredOnBody() {
        FakeController controller = new FakeController() {
            @Route(method = HttpMethod.GET, uri = "/")
            public Result index(@Valid @NotNull @Body Form form) {
                return ok();
            }
        };

        router.bindController(controller);

        final org.wisdom.api.router.Route route = router.getRouteFor(HttpMethod.GET, "/");
        assertThat(route).isNotNull();

        Form form = new Form();
        form.name = "wisdom";
        form.age = 32;
        form.email = "wisdom@wisdom.io";
        // Valid invocation
        Action.ActionResult result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).body(form).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.OK);

        // Invalid invocation
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

        // Invalid invocation
        form.email = null;
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).body(form).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

        // Invalid invocation
        form.email = "wisdom@wisdom.io";
        form.age = 17;
        result = action(new Invocation() {
            @Override
            public Result invoke() throws Throwable {
                return route.invoke();
            }
        }).body(form).invoke();

        assertThat(result.result.getStatusCode()).isEqualTo(Status.BAD_REQUEST);

    }

    public String toString(Action.ActionResult result) {
        try {
            return IOUtils.toString(result.result.getRenderable().render(result.context, result.result));
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve the String form of result `" + result + "`", e);
        }
    }

}
