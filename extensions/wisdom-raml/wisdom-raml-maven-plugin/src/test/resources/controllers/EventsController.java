/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package controllers;

import org.apache.felix.ipojo.annotations.Requires;
import org.jooq.DSLContext;
import org.jug.montpellier.cartridges.events.services.EventsService;
import org.jug.montpellier.core.api.CartridgeSupport;
import org.jug.montpellier.core.api.JugSupport;
import org.jug.montpellier.core.api.NextEventSupport;
import org.jug.montpellier.core.api.PartnerSupport;
import org.jug.montpellier.core.controller.JugController;
import org.jug.montpellier.models.Event;
import org.montpellierjug.store.jooq.tables.daos.EventDao;
import org.montpellierjug.store.jooq.tables.daos.EventpartnerDao;
import org.montpellierjug.store.jooq.tables.daos.SpeakerDao;
import org.montpellierjug.store.jooq.tables.daos.TalkDao;
import org.montpellierjug.store.jooq.tables.interfaces.IEvent;
import org.montpellierjug.store.jooq.tables.interfaces.ISpeaker;
import org.montpellierjug.store.jooq.tables.interfaces.ITalk;
import org.montpellierjug.store.jooq.tables.pojos.Talk;
import org.wisdom.api.annotations.*;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.templates.Template;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Path("/events")
public class EventsController extends JugController {

    @View("events")
    Template template;
    @Requires
    EventsService eventsService;

    public EventsController(@Requires JugSupport jugSupport) {
        super(jugSupport);
    }

    @Route(method = HttpMethod.GET, uri = "")
    public Result events() {
        return eventsService.renderEvents(template(template));
    }

    @Route(method = HttpMethod.GET, uri = "/{id}")
    public Result event(@Parameter("id") Long id) {
        return eventsService.renderEvents(template(template), id);
    }

}
